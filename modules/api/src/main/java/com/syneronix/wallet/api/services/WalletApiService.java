package com.syneronix.wallet.api.services;

import com.syneronix.wallet.api.dto.*;
import com.syneronix.wallet.api.errors.*;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.RequestType;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.domain.TransactionEntity;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.mappers.JsonMapper;
import com.syneronix.wallet.services.IdempotencyService;
import com.syneronix.wallet.services.PolicyService;
import com.syneronix.wallet.services.TransactionService;
import com.syneronix.wallet.services.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletApiService {

    private final WalletService walletService;
    private final IdempotencyApiService idempotencyApiService;
    private final WalletMapper walletMapper;
    private final JsonMapper jsonMapper;
    private final TransactionService transactionService;
    private final PolicyService policyService;

    public WalletResponse createWallet(@Valid CreateWalletRequest request) {
        try {
            log.info("Processing createWallet request. RequestID: {}", request.getRequestId());

            // Check Idempotency
            Optional<WalletResponse> storedResponse = idempotencyApiService.checkIdempotency(
                    request.getRequestId(),
                    request,
                    WalletResponse.class
            );

            if (storedResponse.isPresent()) {
                log.warn("Duplicate request ignored, Idempotency key found for request ID: {}, returning stored response.", request.getRequestId());
                return storedResponse.get();
            }

            // New Request
            idempotencyApiService.create(request.getRequestId(), request, RequestType.CREATE_WALLET, request.getCurrency(), null, null);

            WalletEntity newWalletEntity = walletService.createWallet(request.getUserId(), request.getCurrency());

            WalletResponse walletResponse = walletMapper.toWalletCreateResponse(newWalletEntity, request);

            idempotencyApiService.success(request.getRequestId(), walletResponse, 201);
            log.debug("Wallet created successfully: {}", walletResponse);
            return walletResponse;
        }
        catch (RequestTamperingException e){
            log.error("Attempt to spoof a request: {}", e.getMessage());
            throw new BadRequestException("Invalid request");
        }
        catch (PreviousRequestFailedException e) {
            throw e; // Rethrow to be handled by GlobalExceptionHandler
        }
        catch (Exception e){
            idempotencyApiService.error(request.getRequestId(), e.getMessage(), 500);
            log.error("Error creating wallet: {}", e.getMessage());
            throw new InternalServerErrorException("Failed to create wallet");
        }
    }

    public WalletResponse getWallet(UUID walletId) {
        WalletEntity walletEntity = walletService.findByWalletIdReadOnly(walletId).orElseThrow(()
                -> new WalletNotFoundException(walletId));
        return walletMapper.entityToResponse(walletEntity);
    }

    public TransactionResponse deposit(UUID walletId, @Valid DepositRequest request) {
        try {
            // 1. Check Idempotency FIRST
            Optional<TransactionResponse> storedResponse = idempotencyApiService.checkIdempotency(
                    request.getRequestId(),
                    request,
                    TransactionResponse.class
            );

            if (storedResponse.isPresent()) {
                log.warn("Duplicate deposit request ignored, returning stored response. RequestID: {}", request.getRequestId());
                return storedResponse.get();
            }

            // 2. Create Idempotency Record (PENDING)
            idempotencyApiService.create(request.getRequestId(), request, RequestType.DEPOSIT, request.getCurrency(), null, walletId);

            // 3. Lock Wallet & Validate
            WalletEntity walletEntity = walletService.findWithLockingById(walletId).orElseThrow(()
                    -> new WalletNotFoundException(walletId));

            policyService.validate(walletEntity);

            // 4. Execute Transaction
            TransactionEntity deposit = transactionService.deposit(request.getRequestId(), walletEntity, request.getAmount(), request.getCurrency());
            TransactionResponse response = walletMapper.toTransactionResponse(deposit);

            // 5. Success
            idempotencyApiService.success(request.getRequestId(), response, 200);
            return response;

        } catch (CurrencyMismatchException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 400);
            throw e;
        }
        catch (WalletLockedException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 423);
            throw e;
        }
        catch (RateLimitExceededException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 429);
            throw e;
        }
        catch (PreviousRequestFailedException e) {
            throw e;
        }
        catch (RequestTamperingException e){
            log.error("Attempt to spoof a request with id {}", request.getRequestId());
            throw new BadRequestException("Invalid request");
        }
        catch (Exception e) {
            idempotencyApiService.error(request.getRequestId(),"Error during deposit execution: operation could not be completed.", 500);
            log.error("Error depositing wallet: {}", e.getMessage());
            throw new InternalServerErrorException("Deposit failed");
        }
    }

    public TransactionResponse withdraw(UUID walletId, @Valid WithdrawRequest request) {
        log.info("Processing withdraw request. RequestID: {}", request.getRequestId());
        try {
            // 1. Check Idempotency FIRST
            Optional<TransactionResponse> storedResponse = idempotencyApiService.checkIdempotency(
                    request.getRequestId(),
                    request,
                    TransactionResponse.class
            );

            if (storedResponse.isPresent()) {
                log.warn("Duplicate withdraw request ignored, returning stored response. RequestID: {}", request.getRequestId());
                return storedResponse.get();
            }

            // 2. Create Idempotency Record (PENDING)
            idempotencyApiService.create(request.getRequestId(), request, RequestType.WITHDRAW, request.getCurrency(), null, walletId);

            // 3. Lock Wallet & Validate
            WalletEntity walletEntity = walletService.findWithLockingById(walletId).orElseThrow(()
                    -> new WalletNotFoundException(walletId));

            policyService.validate(walletEntity);

            // 4. Execute Transaction
            TransactionEntity withdraw = transactionService.withdraw(request.getRequestId(), walletEntity, request.getAmount(), request.getCurrency());

            TransactionResponse response = walletMapper.toTransactionResponse(withdraw);

            // 5. Success
            idempotencyApiService.success(request.getRequestId(), response, 200);
            return response;
        }
        catch (CurrencyMismatchException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 400);
            throw e;
        }
        catch (WalletLockedException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 423);
            throw e;
        }
        catch (RateLimitExceededException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 429);
            throw e;
        }
        catch (PreviousRequestFailedException e) {
            throw e;
        }
        catch (RequestTamperingException e){
            log.error("Attempt to spoof a request with id {} reason {}", request.getRequestId() ,e.getMessage());
            throw new BadRequestException("Invalid request");
        }
        catch (Exception e){
            idempotencyApiService.error(request.getRequestId(), "Error during withdraw execution: operation could not be completed.", 500);
            log.error("Error withdrawing wallet: {}", e.getMessage());
            throw new InternalServerErrorException("Withdraw failed");
        }
    }

    public TransactionResponse transfer(@Valid TransferRequest request) {
        log.info("Processing transfer request. RequestID: {}", request.getRequestId());
        try {
            // 1. Check Idempotency
            Optional<TransactionResponse> storedResponse = idempotencyApiService.checkIdempotency(
                    request.getRequestId(),
                    request,
                    TransactionResponse.class
            );

            if (storedResponse.isPresent()) {
                return storedResponse.get();
            }

            // 2. Create Idempotency
            idempotencyApiService.create(request.getRequestId(), request, RequestType.TRANSFER, request.getCurrency(), request.getFromWalletId(), request.getToWalletId());

            // 3. Lock Wallets (Batch lock with currency check)
            List<WalletEntity> wallets = walletService.lockAllByIdsAndCurrencyOrdered(
                    List.of(request.getFromWalletId(), request.getToWalletId()), 
                    request.getCurrency()
            );

            if (wallets.size() != 2) {
                // Either one wallet missing, or currency mismatch
                // We can't easily distinguish without more queries, so generic error or NotFound
                throw new WalletNotFoundException(request.getFromWalletId()); // Or generic "Wallets not found or currency mismatch"
            }

            WalletEntity fromWallet = wallets.stream().filter(w -> w.getId().equals(request.getFromWalletId())).findFirst().orElseThrow();
            WalletEntity toWallet = wallets.stream().filter(w -> w.getId().equals(request.getToWalletId())).findFirst().orElseThrow();

            policyService.validate(fromWallet);
            policyService.validate(toWallet);

            // 4. Execute
            TransactionEntity transfer = transactionService.transfer(request.getRequestId(), fromWallet, toWallet, request.getAmount(), request.getCurrency());

            TransactionResponse response = walletMapper.toTransactionResponse(transfer);

            idempotencyApiService.success(request.getRequestId(), response, 200);
            return response;

        } catch (CurrencyMismatchException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 400);
            throw e;
        }
        catch (WalletLockedException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 423);
            throw e;
        }
        catch (RateLimitExceededException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 429);
            throw e;
        }
        catch (PreviousRequestFailedException e) {
            throw e;
        }
        catch (ConflictException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 409);
            throw e;
        }
        catch (RequestTamperingException e){
            log.error("Attempt to spoof a request with id {}", request.getRequestId());
            throw new BadRequestException("Invalid request");
        }
        catch (Exception e) {
            idempotencyApiService.error(request.getRequestId(), e.getMessage(), 500);
            log.error("Error transferring funds: {}", e.getMessage());
            throw new InternalServerErrorException("Transfer failed");
        }
    }

    public List<TransactionResponse> getTransactions(UUID walletId) {
        if (walletService.findByWalletIdReadOnly(walletId).isEmpty()) {
            throw new WalletNotFoundException(walletId);
        }
        return transactionService.getTransactions(walletId).stream()
                .map(walletMapper::toTransactionResponse)
                .collect(Collectors.toList());
    }
}

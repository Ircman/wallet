package com.syneronix.wallet.api.services;

import com.syneronix.wallet.api.dto.AbstractBaseRequest;
import com.syneronix.wallet.api.dto.wallet.*;
import com.syneronix.wallet.api.errors.*;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.RequestType;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.domain.TransactionEntity;
import com.syneronix.wallet.domain.WalletEntity;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletApiService {

    private final WalletService walletService;
    private final IdempotencyApiService idempotencyApiService;
    private final WalletMapper walletMapper;
    private final TransactionService transactionService;
    private final PolicyService policyService;

    public WalletResponse createWallet(@Valid CreateWalletRequest request) {
        log.info("Processing createWallet request. RequestID: {}", request.getRequestId());

        return proccessRequest(
                request,
                RequestType.CREATE_WALLET,
                request.getCurrency(),
                null, null,
                WalletResponse.class,
                201,
                () -> {
                    WalletEntity newWalletEntity = walletService.createWallet(request.getUserId(), request.getCurrency());
                    return walletMapper.toWalletCreateResponse(newWalletEntity, request);
                }
        );
    }

    public WalletResponse getWallet(UUID walletId) {
        WalletEntity walletEntity = walletService.findByWalletIdReadOnly(walletId).orElseThrow(()
                -> new WalletNotFoundException(walletId));
        return walletMapper.entityToResponse(walletEntity);
    }

    public TransactionResponse deposit(UUID walletId, @Valid DepositRequest request) {
        if (walletService.findByWalletIdReadOnly(walletId).isEmpty()) {
            throw new WalletNotFoundException(walletId);
        }

        return proccessRequest(
                request,
                RequestType.DEPOSIT,
                request.getCurrency(),
                null, walletId, // from=null, to=walletId
                TransactionResponse.class,
                200,
                () -> {
                    WalletEntity walletEntity = walletService.findWithLockingById(walletId).orElseThrow(()
                            -> new WalletNotFoundException(walletId));

                    policyService.validate(walletEntity);

                    TransactionEntity deposit = transactionService.deposit(request.getRequestId(), walletEntity, request.getAmount(), request.getCurrency());

                    if (deposit.getStatus() == TransactionStatus.FAILED) {
                        throw new TransactionFailedException(deposit.getFailureReason());
                    }

                    return walletMapper.toTransactionResponse(deposit);
                }
        );
    }

    public TransactionResponse withdraw(UUID walletId, @Valid WithdrawRequest request) {
        log.info("Processing withdraw request. RequestID: {}", request.getRequestId());
        if (walletService.findByWalletIdReadOnly(walletId).isEmpty()) {
            throw new WalletNotFoundException(walletId);
        }

        return proccessRequest(
                request,
                RequestType.WITHDRAW,
                request.getCurrency(),
                walletId, null, // from=walletId, to=null
                TransactionResponse.class,
                200,
                () -> {
                    WalletEntity walletEntity = walletService.findWithLockingById(walletId).orElseThrow(()
                            -> new WalletNotFoundException(walletId));

                    policyService.validate(walletEntity);

                    TransactionEntity withdraw = transactionService.withdraw(request.getRequestId(), walletEntity, request.getAmount(), request.getCurrency());

                    if (withdraw.getStatus() == TransactionStatus.FAILED) {
                        throw new TransactionFailedException(withdraw.getFailureReason());
                    }

                    return walletMapper.toTransactionResponse(withdraw);
                }
        );
    }

    public TransactionResponse transfer(@Valid TransferRequest request) {
        log.info("Processing transfer request. RequestID: {}", request.getRequestId());

        return proccessRequest(
                request,
                RequestType.TRANSFER,
                request.getCurrency(),
                request.getFromWalletId(), request.getToWalletId(),
                TransactionResponse.class,
                200,
                () -> {
                    List<WalletEntity> wallets = walletService.lockAllByIdsAndCurrencyOrdered(
                            List.of(request.getFromWalletId(), request.getToWalletId()),
                            request.getCurrency()
                    );

                    if (wallets.isEmpty()) {
                        throw new WalletNotFoundException(request.getFromWalletId(), request.getToWalletId());
                    }

                    if (wallets.size() != 2) {
                        List<UUID> foundIds = wallets.stream().map(WalletEntity::getId).toList();
                        if (!foundIds.contains(request.getFromWalletId()))
                            throw new WalletNotFoundException(request.getFromWalletId());
                        if (!foundIds.contains(request.getToWalletId()))
                            throw new WalletNotFoundException(request.getToWalletId());
                    }

                    WalletEntity fromWallet = wallets.stream().filter(w -> w.getId().equals(request.getFromWalletId())).findFirst().orElseThrow();
                    WalletEntity toWallet = wallets.stream().filter(w -> w.getId().equals(request.getToWalletId())).findFirst().orElseThrow();

                    if (fromWallet.getId().equals(toWallet.getId())) {
                        throw new BadRequestException("Cannot transfer to the same wallet");
                    }

                    policyService.validate(fromWallet);
                    policyService.validate(toWallet);

                    TransactionEntity transfer = transactionService.transfer(request.getRequestId(), fromWallet, toWallet, request.getAmount(), request.getCurrency());

                    if (transfer.getStatus() == TransactionStatus.FAILED) {
                        throw new TransactionFailedException(transfer.getFailureReason());
                    }

                    return walletMapper.toTransactionResponse(transfer);
                }
        );
    }

    public List<TransactionResponse> getTransactions(UUID walletId) {
        if (walletService.findByWalletIdReadOnly(walletId).isEmpty()) {
            throw new WalletNotFoundException(walletId);
        }
        return transactionService.getTransactions(walletId).stream()
                .map(walletMapper::toTransactionResponse)
                .collect(Collectors.toList());
    }

    private <T, R extends AbstractBaseRequest> T proccessRequest(
            R request,
            RequestType requestType,
            Currency currency,
            UUID fromWalletId,
            UUID toWalletId,
            Class<T> responseType,
            int successStatus,
            Supplier<T> businessLogic
    ) {
        try {
            Optional<T> storedResponse = idempotencyApiService.checkIdempotency(
                    request.getRequestId(),
                    request,
                    responseType
            );

            if (storedResponse.isPresent()) {
                return storedResponse.get();
            }

            idempotencyApiService.create(request.getRequestId(), request, requestType, currency, fromWalletId, toWalletId);

            T response = businessLogic.get();

            idempotencyApiService.success(request.getRequestId(), response, successStatus);
            return response;

        } catch (CurrencyMismatchException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 400);
            throw e;
        } catch (TransactionFailedException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 422);
            throw e;
        } catch (WalletLockedException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 423);
            throw e;
        } catch (RateLimitExceededException e) {
            idempotencyApiService.rejected(request.getRequestId(), request, e.getMessage(), 429);
            throw e;
        } catch (PreviousRequestFailedException e) {
            throw e;
        } catch (RequestTamperingException e) {
            log.error("Attempt to spoof a request with id {}", request.getRequestId());
            throw new BadRequestException("Invalid request");
        } catch (Exception e) {
            idempotencyApiService.error(request.getRequestId(), e.getMessage(), 500);
            log.error("Error processing request {}: {}", request.getRequestId(), e.getMessage());
            throw new InternalServerErrorException("Operation failed");
        }
    }
}

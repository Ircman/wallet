package com.syneronix.wallet.api.services;

import com.syneronix.wallet.api.dto.wallet.*;
import com.syneronix.wallet.api.errors.InternalServerErrorException;
import com.syneronix.wallet.api.errors.PreviousRequestFailedException;
import com.syneronix.wallet.api.errors.RequestTamperingException;
import com.syneronix.wallet.common.*;
import com.syneronix.wallet.domain.IdempotencyKeyEntity;
import com.syneronix.wallet.mappers.JsonMapper;
import com.syneronix.wallet.services.IdempotencyService;
import com.syneronix.wallet.utils.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IdempotencyApiService {

    private final IdempotencyService idempotencyService;
    private final JsonMapper jsonMapper;
    private final WalletMapper walletMapper;

    public <T> Optional<T> checkIdempotency(UUID requestId, Object requestObject, Class<T> responseType) {
        Optional<IdempotencyKeyEntity> entityOpt = idempotencyService.findByRequestId(requestId);

        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        IdempotencyKeyEntity entity = entityOpt.get();

        // 1. Hash Check
        String jsonRequest = jsonMapper.toJson(requestObject);
        String currentHash = HashUtil.calculateSha256(jsonRequest);
        if (!currentHash.equals(entity.getRequestHash())) {
            throw new RequestTamperingException(requestId);
        }

        // 2. Status Check
        if (entity.getStatus() == TransactionStatus.FAILED || entity.getStatus() == TransactionStatus.REJECTED) {
            throw new PreviousRequestFailedException(entity.getHttpStatusCode(), entity.getFailReason());
        }

        // 3. Pending Check
        if (entity.getStatus() == TransactionStatus.PENDING) {
            return Optional.of(createPendingResponse(entity.getRequestType(), requestObject, responseType));
        }

        // 4. Completed Check
        String responseBody = entity.getResponseBody();
        if (StringUtils.isBlank(responseBody)) {
            log.error("Data Integrity Error: Idempotency key {} is COMPLETED but response body is missing.", requestId);
            throw new InternalServerErrorException("System error: Invalid idempotency state");
        }

        return Optional.ofNullable(jsonMapper.fromJson(responseBody, responseType));
    }

    private <T> T createPendingResponse(RequestType type, Object request, Class<T> responseType) {
        switch (type) {
            case CREATE_WALLET:
                if (responseType.isAssignableFrom(WalletResponse.class)) {
                    WalletResponse wr = walletMapper.toWalletCreateResponse((CreateWalletRequest) request);
                    wr.setStatus(WalletStatus.PENDING);
                    return responseType.cast(wr);
                }
                throw new IllegalStateException("Response type mismatch for CREATE_WALLET. Expected WalletResponse, got " + responseType.getName());
                
            case DEPOSIT:
                if (responseType.isAssignableFrom(TransactionResponse.class)) {
                    TransactionResponse tr = walletMapper.toTransactionResponse((DepositRequest) request);
                    tr.setStatus(TransactionStatus.PENDING);
                    tr.setType(TransactionType.DEPOSIT);
                    return responseType.cast(tr);
                }
                throw new IllegalStateException("Response type mismatch for DEPOSIT. Expected TransactionResponse, got " + responseType.getName());

            case TRANSFER:
                if (responseType.isAssignableFrom(TransactionResponse.class)) {
                    TransactionResponse tr = walletMapper.toTransactionResponse((TransferRequest) request);
                    tr.setStatus(TransactionStatus.PENDING);
                    tr.setType(TransactionType.TRANSFER);
                    return responseType.cast(tr);
                }
                throw new IllegalStateException("Response type mismatch for TRANSFER. Expected TransactionResponse, got " + responseType.getName());

            default:
                throw new IllegalStateException("Unsupported request type for pending response: " + type);
        }
    }

    public void create(UUID requestId, Object requestBody, RequestType type, Currency currency, UUID fromWalletId, UUID toWalletId) {
        idempotencyService.create(requestId, requestBody, type, currency, fromWalletId, toWalletId);
    }

    public void success(UUID requestId, Object responseBody, int httpStatus) {
        idempotencyService.success(requestId, responseBody, httpStatus);
    }

    public void error(UUID requestId, String reason, int httpStatus) {
        idempotencyService.error(requestId, reason, httpStatus);
    }

    public void rejected(UUID requestId, Object responseBody, String reason, int httpStatus) {
        idempotencyService.rejected(requestId, responseBody, reason, httpStatus);
    }
}

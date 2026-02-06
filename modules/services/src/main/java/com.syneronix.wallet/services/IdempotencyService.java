package com.syneronix.wallet.services;


import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.RequestType;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.domain.IdempotencyKeyEntity;
import com.syneronix.wallet.domain.IdempotencyKeyRepository;
import com.syneronix.wallet.mappers.JsonMapper;
import com.syneronix.wallet.utils.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final JsonMapper jsonMapper;

    @Transactional(readOnly = true)
    public Optional<IdempotencyKeyEntity> findByRequestId(UUID requestId) {
        return idempotencyKeyRepository.findByRequestId(requestId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(UUID requestId, Object requestBody, RequestType type, Currency currency, UUID fromWalletId, UUID toWalletId) {
        if (idempotencyKeyRepository.existsByRequestId(requestId)) {
            return;
        }

        IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
        entity.setRequestId(requestId);
        entity.setRequestType(type);
        entity.setCurrency(currency);
        entity.setFromWalletId(fromWalletId);
        entity.setToWalletId(toWalletId);
        entity.setStatus(TransactionStatus.PENDING);

        String requestJson = jsonMapper.toJson(requestBody);
        String requestHash = HashUtil.calculateSha256(requestJson);
        entity.setRequestBody(requestJson);
        entity.setRequestHash(requestHash);

        idempotencyKeyRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(UUID requestId, Object responseBody, int httpStatus) {
        IdempotencyKeyEntity entity = idempotencyKeyRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Idempotency key not found for success update: " + requestId));
        entity.setStatus(TransactionStatus.COMPLETED);
        entity.setHttpStatusCode(httpStatus);
        entity.setResponseBody(jsonMapper.toJson(responseBody));

        idempotencyKeyRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rejected(UUID requestId, Object responseBody, String reason, int httpStatus) {
        IdempotencyKeyEntity entity = idempotencyKeyRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Idempotency key not found for success update: " + requestId));
        entity.setStatus(TransactionStatus.REJECTED);
        entity.setHttpStatusCode(httpStatus);
        entity.setResponseBody(jsonMapper.toJson(responseBody));
        entity.setFailReason(truncateReason(reason));
        idempotencyKeyRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void error(UUID requestId, String reason, int httpStatus) {
        IdempotencyKeyEntity entity = idempotencyKeyRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Idempotency key not found for error update: " + requestId));

        entity.setStatus(TransactionStatus.FAILED);
        entity.setHttpStatusCode(httpStatus);
        entity.setFailReason(truncateReason(reason));

        idempotencyKeyRepository.save(entity);
    }


    private String truncateReason(String reason) {
        if (StringUtils.isBlank(reason)) {
            return null;
        }
        return reason.length() > 255
                ? reason.substring(0, 255)
                : reason;
    }
}

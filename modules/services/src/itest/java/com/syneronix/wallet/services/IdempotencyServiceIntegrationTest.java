package com.syneronix.wallet.services;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.RequestType;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.domain.IdempotencyKeyEntity;
import com.syneronix.wallet.domain.IdempotencyKeyRepository;
import com.syneronix.wallet.testing.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

class IdempotencyServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    private UUID requestId;

    @BeforeEach
    void setUp() {
        idempotencyKeyRepository.deleteAll();
        requestId = uuid();
    }

    @Test
    void create_shouldCreatePendingRecord() {
        Map<String, String> requestBody = Map.of("key", "value");

        idempotencyService.create(requestId, requestBody, RequestType.DEPOSIT, Currency.USD, null, null);

        IdempotencyKeyEntity entity = idempotencyKeyRepository.findByRequestId(requestId).orElseThrow();
        assertThat(entity.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(entity.getRequestBody()).isEqualTo("{\"key\": \"value\"}");
    }

    @Test
    void success_shouldUpdateStatusToCompleted() {
        idempotencyService.create(requestId, Map.of(), RequestType.DEPOSIT, Currency.USD, null, null);

        idempotencyService.success(requestId, Map.of("status", "ok"), 200);

        IdempotencyKeyEntity entity = idempotencyKeyRepository.findByRequestId(requestId).orElseThrow();
        assertThat(entity.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(entity.getResponseBody()).isEqualTo("{\"status\": \"ok\"}");
        assertThat(entity.getHttpStatusCode()).isEqualTo(200);
    }

    @Test
    void error_shouldUpdateStatusToFailed() {
        idempotencyService.create(requestId, Map.of(), RequestType.DEPOSIT, Currency.USD, null, null);

        idempotencyService.error(requestId, "Something went wrong", 500);

        IdempotencyKeyEntity entity = idempotencyKeyRepository.findByRequestId(requestId).orElseThrow();
        assertThat(entity.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(entity.getFailReason()).isEqualTo("Something went wrong");
        assertThat(entity.getHttpStatusCode()).isEqualTo(500);
    }
}

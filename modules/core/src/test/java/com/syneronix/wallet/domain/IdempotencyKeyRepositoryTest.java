package com.syneronix.wallet.domain;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.RequestType;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.testing.BaseH2InMemoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;


class IdempotencyKeyRepositoryTest extends BaseH2InMemoryTest {

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Test
    void save_shouldPersistIdempotencyKey() {
        IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
        entity.setRequestId(java.util.UUID.randomUUID());
        entity.setRequestType(RequestType.DEPOSIT);
        entity.setCurrency(Currency.USD);
        entity.setStatus(TransactionStatus.PENDING);
        entity.setRequestBody("{}");
        entity.setHttpStatusCode(200);

        IdempotencyKeyEntity saved = idempotencyKeyRepository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(idempotencyKeyRepository.existsByRequestId(entity.getRequestId())).isTrue();
    }
}

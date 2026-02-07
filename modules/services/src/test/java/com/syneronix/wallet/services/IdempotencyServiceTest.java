package com.syneronix.wallet.services;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.RequestType;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.domain.IdempotencyKeyEntity;
import com.syneronix.wallet.domain.IdempotencyKeyRepository;
import com.syneronix.wallet.mappers.JsonMapper;
import com.syneronix.wallet.testing.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdempotencyServiceTest extends BaseUnitTest {

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private IdempotencyService idempotencyService;

    private UUID requestId;
    private Object requestObject;
    private IdempotencyKeyEntity entity;

    @BeforeEach
    void setUp() {
        requestId = uuid();
        requestObject = new Object();
        entity = new IdempotencyKeyEntity();
        entity.setRequestId(requestId);
    }

    @Test
    void create_shouldSaveEntity() {
        when(idempotencyKeyRepository.existsByRequestId(requestId)).thenReturn(false);
        when(jsonMapper.toJson(requestObject)).thenReturn("{}");

        idempotencyService.create(requestId, requestObject, RequestType.DEPOSIT, Currency.USD, null, null);

        ArgumentCaptor<IdempotencyKeyEntity> captor = ArgumentCaptor.forClass(IdempotencyKeyEntity.class);
        verify(idempotencyKeyRepository).save(captor.capture());

        IdempotencyKeyEntity saved = captor.getValue();
        assertThat(saved.getRequestId()).isEqualTo(requestId);
        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void success_shouldUpdateEntity() {
        when(idempotencyKeyRepository.findByRequestId(requestId)).thenReturn(Optional.of(entity));
        when(jsonMapper.toJson(any())).thenReturn("{}");

        idempotencyService.success(requestId, new Object(), 200);

        verify(idempotencyKeyRepository).save(entity);
        assertThat(entity.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }
}

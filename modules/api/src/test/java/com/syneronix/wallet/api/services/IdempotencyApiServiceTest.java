package com.syneronix.wallet.api.services;

import com.syneronix.wallet.api.dto.wallet.CreateWalletRequest;
import com.syneronix.wallet.api.dto.wallet.WalletMapper;
import com.syneronix.wallet.api.dto.wallet.WalletResponse;
import com.syneronix.wallet.api.errors.PreviousRequestFailedException;
import com.syneronix.wallet.api.errors.RequestTamperingException;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.RequestType;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.IdempotencyKeyEntity;
import com.syneronix.wallet.mappers.JsonMapper;
import com.syneronix.wallet.services.IdempotencyService;
import com.syneronix.wallet.testing.BaseUnitTest;
import com.syneronix.wallet.utils.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdempotencyApiServiceTest extends BaseUnitTest {

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private WalletMapper walletMapper;

    @InjectMocks
    private IdempotencyApiService idempotencyApiService;

    private UUID requestId;

    @BeforeEach
    void setup() {
        requestId = uuid();
    }


    @Test
    void checkIdempotency_shouldReturnEmpty_whenKeyNotFound() {

        when(idempotencyService.findByRequestId(requestId)).thenReturn(Optional.empty());

        Optional<WalletResponse> result = idempotencyApiService.checkIdempotency(requestId, new Object(), WalletResponse.class);

        assertThat(result).isEmpty();
    }

    @Test
    void checkIdempotency_shouldThrow_whenHashMismatch() {
        Object request = new Object();
        String json = "{}";

        IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
        entity.setRequestHash("differentHash");

        when(idempotencyService.findByRequestId(requestId)).thenReturn(Optional.of(entity));
        when(jsonMapper.toJson(request)).thenReturn(json);

        assertThrows(RequestTamperingException.class, () ->
                idempotencyApiService.checkIdempotency(requestId, request, WalletResponse.class)
        );
    }

    @Test
    void checkIdempotency_shouldThrow_whenStatusFailed() {
        Object request = new Object();
        String json = "{}";
        String hash = HashUtil.calculateSha256(json);

        IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
        entity.setRequestHash(hash);
        entity.setStatus(TransactionStatus.FAILED);
        entity.setFailReason("Error");
        entity.setHttpStatusCode(500);

        when(idempotencyService.findByRequestId(requestId)).thenReturn(Optional.of(entity));
        when(jsonMapper.toJson(request)).thenReturn(json);

        assertThrows(PreviousRequestFailedException.class, () ->
                idempotencyApiService.checkIdempotency(requestId, request, WalletResponse.class)
        );
    }

    @Test
    void checkIdempotency_shouldReturnPendingResponse_whenStatusPending() {
        CreateWalletRequest request = new CreateWalletRequest();
        String json = "{}";
        String hash = HashUtil.calculateSha256(json);

        IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
        entity.setRequestHash(hash);
        entity.setStatus(TransactionStatus.PENDING);
        entity.setRequestType(RequestType.CREATE_WALLET);

        WalletResponse pendingResponse = new WalletResponse();
        pendingResponse.setStatus(WalletStatus.PENDING);

        when(idempotencyService.findByRequestId(requestId)).thenReturn(Optional.of(entity));
        when(jsonMapper.toJson(request)).thenReturn(json);
        when(walletMapper.toWalletCreateResponse(request)).thenReturn(pendingResponse);

        Optional<WalletResponse> result = idempotencyApiService.checkIdempotency(requestId, request, WalletResponse.class);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(WalletStatus.PENDING);
    }

    @Test
    void checkIdempotency_shouldReturnCompletedResponse_whenStatusCompleted() {
        Object request = new Object();
        String json = "{}";
        String hash = HashUtil.calculateSha256(json);
        String responseJson = "{\"id\":\"123\"}";

        IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
        entity.setRequestHash(hash);
        entity.setStatus(TransactionStatus.COMPLETED);
        entity.setResponseBody(responseJson);

        WalletResponse completedResponse = new WalletResponse();

        when(idempotencyService.findByRequestId(requestId)).thenReturn(Optional.of(entity));
        when(jsonMapper.toJson(request)).thenReturn(json);
        when(jsonMapper.fromJson(responseJson, WalletResponse.class)).thenReturn(completedResponse);

        Optional<WalletResponse> result = idempotencyApiService.checkIdempotency(requestId, request, WalletResponse.class);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(completedResponse);
    }

    @Test
    void create_shouldDelegateToService() {
        Object request = new Object();
        idempotencyApiService.create(requestId, request, RequestType.DEPOSIT, Currency.USD, null, null);
        verify(idempotencyService).create(eq(requestId), eq(request), eq(RequestType.DEPOSIT), eq(Currency.USD), isNull(), isNull());
    }

    @Test
    void success_shouldDelegateToService() {
        Object response = new Object();
        idempotencyApiService.success(requestId, response, 200);
        verify(idempotencyService).success(requestId, response, 200);
    }

    @Test
    void error_shouldDelegateToService() {
        idempotencyApiService.error(requestId, "error", 500);
        verify(idempotencyService).error(requestId, "error", 500);
    }

    @Test
    void rejected_shouldDelegateToService() {
        Object request = new Object();
        idempotencyApiService.rejected(requestId, request, "reason", 400);
        verify(idempotencyService).rejected(requestId, request, "reason", 400);
    }
}

package com.syneronix.wallet.api.services;

import com.syneronix.wallet.api.dto.wallet.*;
import com.syneronix.wallet.api.errors.TransactionFailedException;
import com.syneronix.wallet.api.errors.WalletNotFoundException;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.RequestType;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.domain.TransactionEntity;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.services.PolicyService;
import com.syneronix.wallet.services.TransactionService;
import com.syneronix.wallet.services.WalletService;
import com.syneronix.wallet.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WalletApiServiceTest extends BaseUnitTest {

    @Mock
    private WalletService walletService;
    @Mock
    private IdempotencyApiService idempotencyApiService;
    @Mock
    private WalletMapper walletMapper;
    @Mock
    private TransactionService transactionService;
    @Mock
    private PolicyService policyService;

    @InjectMocks
    private WalletApiService walletApiService;

    @Test
    void createWallet_shouldSucceed() {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setRequestId(uuid());
        request.setUserId(uuid());
        request.setCurrency(Currency.USD);

        WalletEntity walletEntity = new WalletEntity();
        WalletResponse walletResponse = new WalletResponse();

        when(idempotencyApiService.checkIdempotency(any(), any(), any())).thenReturn(Optional.empty());
        when(walletService.createWallet(any(), any())).thenReturn(walletEntity);
        when(walletMapper.toWalletCreateResponse(any(), any())).thenReturn(walletResponse);

        WalletResponse result = walletApiService.createWallet(request);

        assertThat(result).isEqualTo(walletResponse);
        verify(idempotencyApiService).create(eq(request.getRequestId()), eq(request), eq(RequestType.CREATE_WALLET), eq(Currency.USD), isNull(), isNull());
        verify(idempotencyApiService).success(eq(request.getRequestId()), eq(walletResponse), eq(201));
    }

    @Test
    void deposit_shouldSucceed() {
        UUID walletId = uuid();
        DepositRequest request = new DepositRequest();
        request.setRequestId(uuid());
        request.setAmount(BigDecimal.TEN);
        request.setCurrency(Currency.USD);

        WalletEntity walletEntity = new WalletEntity();
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setStatus(TransactionStatus.COMPLETED);
        TransactionResponse transactionResponse = new TransactionResponse();

        when(idempotencyApiService.checkIdempotency(any(), any(), any())).thenReturn(Optional.empty());
        when(walletService.findWithLockingById(walletId)).thenReturn(Optional.of(walletEntity));
        when(transactionService.deposit(any(), any(), any(), any())).thenReturn(transactionEntity);
        when(walletMapper.toTransactionResponse(transactionEntity)).thenReturn(transactionResponse);

        TransactionResponse result = walletApiService.deposit(walletId, request);

        assertThat(result).isEqualTo(transactionResponse);
        verify(policyService).validate(walletEntity);
        verify(idempotencyApiService).success(eq(request.getRequestId()), eq(transactionResponse), eq(200));
    }

    @Test
    void deposit_shouldThrow_whenWalletNotFound() {
        UUID walletId = uuid();
        DepositRequest request = new DepositRequest();
        request.setRequestId(uuid());

        when(idempotencyApiService.checkIdempotency(any(), any(), any())).thenReturn(Optional.empty());
        when(walletService.findWithLockingById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletApiService.deposit(walletId, request));

        // Verify idempotency rejected with 404
        verify(idempotencyApiService).rejected(eq(request.getRequestId()), eq(request), anyString(), eq(404));
    }

    @Test
    void deposit_shouldThrow_whenTransactionFailed() {
        UUID walletId = uuid();
        DepositRequest request = new DepositRequest();
        request.setRequestId(uuid());
        request.setCurrency(Currency.USD);

        WalletEntity walletEntity = new WalletEntity();
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setStatus(TransactionStatus.FAILED);
        transactionEntity.setFailureReason("Error");

        when(idempotencyApiService.checkIdempotency(any(), any(), any())).thenReturn(Optional.empty());
        when(walletService.findWithLockingById(walletId)).thenReturn(Optional.of(walletEntity));
        when(transactionService.deposit(any(), any(), any(), any())).thenReturn(transactionEntity);

        assertThrows(TransactionFailedException.class, () -> walletApiService.deposit(walletId, request));

        verify(idempotencyApiService).rejected(eq(request.getRequestId()), eq(request), eq("Error"), eq(422));
    }

    @Test
    void transfer_shouldSucceed() {
        TransferRequest request = new TransferRequest();
        request.setRequestId(uuid());
        request.setFromWalletId(uuid());
        request.setToWalletId(uuid());
        request.setCurrency(Currency.USD);
        request.setAmount(BigDecimal.TEN);

        WalletEntity fromWallet = new WalletEntity();
        fromWallet.setId(request.getFromWalletId());
        WalletEntity toWallet = new WalletEntity();
        toWallet.setId(request.getToWalletId());

        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setStatus(TransactionStatus.COMPLETED);
        TransactionResponse transactionResponse = new TransactionResponse();

        when(idempotencyApiService.checkIdempotency(any(), any(), any())).thenReturn(Optional.empty());

        when(walletService.lockAllByIdsAndCurrencyOrdered(any(), any())).thenReturn(List.of(fromWallet, toWallet));

        when(transactionService.transfer(any(), any(), any(), any(), any())).thenReturn(transactionEntity);
        when(walletMapper.toTransactionResponse(transactionEntity)).thenReturn(transactionResponse);

        TransactionResponse result = walletApiService.transfer(request);

        assertThat(result).isEqualTo(transactionResponse);
        verify(policyService).validate(fromWallet);
        verify(policyService).validate(toWallet);
        verify(idempotencyApiService).success(eq(request.getRequestId()), eq(transactionResponse), eq(200));
    }
}

package com.syneronix.wallet.api.services;

import com.syneronix.wallet.api.dto.wallet.DepositRequest;
import com.syneronix.wallet.api.dto.wallet.TransactionResponse;
import com.syneronix.wallet.api.errors.InternalServerErrorException;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.*;
import com.syneronix.wallet.services.TransactionService;
import com.syneronix.wallet.testing.BaseMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class WalletApiServiceTest extends BaseMockMvcTest {

    @Autowired
    private WalletApiService walletApiService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    @SpyBean
    private TransactionService transactionService;

    private WalletEntity testWallet;

    @BeforeEach
    void setUp() {
        idempotencyKeyRepository.deleteAll();
        ledgerEntryRepository.deleteAll();
        transactionRepository.deleteAll();
        blacklistRepository.deleteAll();
        walletRepository.deleteAll();

        testWallet = new WalletEntity();
        testWallet.setUserId(uuid());
        testWallet.setCurrency(Currency.USD);
        testWallet.setBalance(BigDecimal.valueOf(100));
        testWallet.setStatus(WalletStatus.ACTIVE);
        testWallet = walletRepository.save(testWallet);
    }

    @Test
    void deposit_shouldSucceed_andCreateIdempotencyRecord() {
        DepositRequest request = createDepositRequest(BigDecimal.valueOf(50));

        TransactionResponse response = walletApiService.deposit(testWallet.getId(), request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50));

        WalletEntity updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));

        IdempotencyKeyEntity idempotencyKey = idempotencyKeyRepository.findByRequestId(request.getRequestId()).orElseThrow();
        assertThat(idempotencyKey.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void deposit_shouldReturnCachedResponse_whenDuplicateRequest() {
        DepositRequest request = createDepositRequest(BigDecimal.valueOf(50));

        TransactionResponse response1 = walletApiService.deposit(testWallet.getId(), request);

        TransactionResponse response2 = walletApiService.deposit(testWallet.getId(), request);

        assertThat(response2).usingRecursiveComparison().isEqualTo(response1);

        WalletEntity updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    @Test
    void deposit_shouldRollbackTransaction_andSetIdempotencyToFailed_whenExceptionOccurs() {
        DepositRequest request = createDepositRequest(BigDecimal.valueOf(50));

        doThrow(new RuntimeException("Simulated DB Error"))
                .when(transactionService).deposit(any(), any(), any(), any());

        assertThrows(InternalServerErrorException.class, () -> walletApiService.deposit(testWallet.getId(), request));

        WalletEntity updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));

        IdempotencyKeyEntity idempotencyKey = idempotencyKeyRepository.findByRequestId(request.getRequestId()).orElseThrow();
        assertThat(idempotencyKey.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(idempotencyKey.getFailReason()).contains("Simulated DB Error");
    }

    private DepositRequest createDepositRequest(BigDecimal amount) {
        DepositRequest request = new DepositRequest();
        request.setAmount(amount);
        request.setCurrency(Currency.USD);
        request.setRequestId(uuid());
        request.setTimestamp(Instant.now());
        return request;
    }
}

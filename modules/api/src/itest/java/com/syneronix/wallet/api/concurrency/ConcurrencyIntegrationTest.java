package com.syneronix.wallet.api.concurrency;

import com.syneronix.wallet.api.dto.wallet.TransactionResponse;
import com.syneronix.wallet.api.dto.wallet.TransferRequest;
import com.syneronix.wallet.api.dto.wallet.WithdrawRequest;
import com.syneronix.wallet.api.services.WalletApiService;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.*;
import com.syneronix.wallet.testing.BaseMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrencyIntegrationTest extends BaseMockMvcTest {

    @Autowired
    private WalletApiService walletApiService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    private WalletEntity senderWallet;
    private WalletEntity receiverWallet;

    @BeforeEach
    void setUp() {
        idempotencyKeyRepository.deleteAll();
        ledgerEntryRepository.deleteAll();
        transactionRepository.deleteAll();
        blacklistRepository.deleteAll();
        walletRepository.deleteAll();

        senderWallet = createWallet(BigDecimal.valueOf(100));
        receiverWallet = createWallet(BigDecimal.ZERO);
    }

    @Test
    void concurrentWithdrawAndTransfer_shouldPreventDoubleSpending() throws InterruptedException {
        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // Thread 1: Withdraw 100
        executorService.submit(() -> {
            try {
                startLatch.await();
                WithdrawRequest request = new WithdrawRequest();
                request.setAmount(BigDecimal.valueOf(100));
                request.setCurrency(Currency.USD);
                request.setRequestId(UUID.randomUUID());
                request.setTimestamp(Instant.now());

                TransactionResponse response = walletApiService.withdraw(senderWallet.getId(), request);
                if (response.getStatus() == TransactionStatus.COMPLETED) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                completionLatch.countDown();
            }
        });

        // Thread 2: Transfer 100
        executorService.submit(() -> {
            try {
                startLatch.await();
                TransferRequest request = new TransferRequest();
                request.setFromWalletId(senderWallet.getId());
                request.setToWalletId(receiverWallet.getId());
                request.setAmount(BigDecimal.valueOf(100));
                request.setCurrency(Currency.USD);
                request.setRequestId(UUID.randomUUID());
                request.setTimestamp(Instant.now());

                TransactionResponse response = walletApiService.transfer(request);
                if (response.getStatus() == TransactionStatus.COMPLETED) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                completionLatch.countDown();
            }
        });

        startLatch.countDown(); // Start both threads
        boolean completed = completionLatch.await(5, TimeUnit.SECONDS); // Wait for completion
        executorService.shutdown();

        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);

        WalletEntity updatedSender = walletRepository.findById(senderWallet.getId()).orElseThrow();
        assertThat(updatedSender.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private WalletEntity createWallet(BigDecimal balance) {
        WalletEntity walletEntity = new WalletEntity();
        walletEntity.setUserId(UUID.randomUUID());
        walletEntity.setCurrency(Currency.USD);
        walletEntity.setBalance(balance);
        walletEntity.setStatus(WalletStatus.ACTIVE);
        return walletRepository.save(walletEntity);
    }
}

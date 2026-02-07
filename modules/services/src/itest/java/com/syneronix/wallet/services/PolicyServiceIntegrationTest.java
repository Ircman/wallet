package com.syneronix.wallet.services;

import com.syneronix.wallet.api.errors.RateLimitExceededException;
import com.syneronix.wallet.api.errors.WalletLockedException;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.TransactionType;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.*;
import com.syneronix.wallet.testing.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicyServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    private WalletEntity testWallet;

    @BeforeEach
    void setUp() {
        ledgerEntryRepository.deleteAll();
        transactionRepository.deleteAll();
        blacklistRepository.deleteAll();
        walletRepository.deleteAll();

        testWallet = new WalletEntity();
        testWallet.setUserId(uuid());
        testWallet.setCurrency(Currency.USD);
        testWallet.setBalance(BigDecimal.ZERO);
        testWallet.setStatus(WalletStatus.ACTIVE);
        testWallet = walletRepository.save(testWallet);
    }

    @Test
    void verifyRateLimit_shouldPass_whenUnderLimit() {
        // Limit is 5 per 1 minute (from application-itest.yml)
        createTransactions(4);
        assertDoesNotThrow(() -> policyService.verifyRateLimit(testWallet.getId()));
    }

    @Test
    void verifyRateLimit_shouldThrow_whenOverLimit() {
        createTransactions(5);
        assertThrows(RateLimitExceededException.class, () -> policyService.verifyRateLimit(testWallet.getId()));
    }

    @Test
    void verifyBlacklist_shouldPass_whenNotBlacklisted() {
        assertDoesNotThrow(() -> policyService.verifyBlacklist(testWallet));
        assertDoesNotThrow(() -> policyService.verifyBlacklist(testWallet.getId()));
    }

    @Test
    void verifyBlacklist_shouldThrow_whenBlacklisted() {
        BlacklistEntity blacklistEntity = new BlacklistEntity();
        blacklistEntity.setWalletId(testWallet.getId());
        blacklistEntity.setReason("Test");
        blacklistRepository.save(blacklistEntity);

        assertThrows(WalletLockedException.class, () -> policyService.verifyBlacklist(testWallet.getId()));
    }

    @Test
    void verifyBlacklist_shouldThrow_whenWalletStatusSuspended() {
        testWallet.setStatus(WalletStatus.SUSPENDED);
        walletRepository.save(testWallet);

        assertThrows(WalletLockedException.class, () -> policyService.verifyBlacklist(testWallet));
    }

    private void createTransactions(int count) {
        for (int i = 0; i < count; i++) {
            TransactionEntity transaction = new TransactionEntity();
            transaction.setRequestId(uuid());
            transaction.setType(TransactionType.WITHDRAW);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setAmount(BigDecimal.TEN);
            transaction.setCurrency(Currency.USD);
            transaction.setFromWallet(testWallet);
            transactionRepository.save(transaction);
        }
    }
}

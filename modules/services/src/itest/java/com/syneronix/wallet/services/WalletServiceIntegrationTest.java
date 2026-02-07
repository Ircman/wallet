package com.syneronix.wallet.services;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.*;
import com.syneronix.wallet.testing.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

class WalletServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;


    @BeforeEach
    void setUp() {
        idempotencyKeyRepository.deleteAll();
        ledgerEntryRepository.deleteAll();
        transactionRepository.deleteAll();
        blacklistRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    void createWallet_shouldSaveWallet() {
        UUID userId = uuid();
        WalletEntity wallet = walletService.createWallet(userId, Currency.USD);

        assertThat(wallet.getId()).isNotNull();
        assertThat(wallet.getUserId()).isEqualTo(userId);
        assertThat(wallet.getCurrency()).isEqualTo(Currency.USD);
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void findWithLockingById_shouldReturnWallet() {
        WalletEntity wallet = new WalletEntity();
        wallet.setUserId(uuid());
        wallet.setCurrency(Currency.USD);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet = walletRepository.save(wallet);

        WalletEntity found = walletService.findWithLockingById(wallet.getId()).orElseThrow();
        assertThat(found.getId()).isEqualTo(wallet.getId());
    }

    @Test
    void lockAllByIdsAndCurrencyOrdered_shouldReturnWallets() {
        WalletEntity w1 = walletService.createWallet(uuid(), Currency.USD);
        WalletEntity w2 = walletService.createWallet(uuid(), Currency.USD);
        walletService.createWallet(uuid(), Currency.EUR); // Should not be found

        List<WalletEntity> found = walletService.lockAllByIdsAndCurrencyOrdered(List.of(w1.getId(), w2.getId()), Currency.USD);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(WalletEntity::getId).containsExactlyInAnyOrder(w1.getId(), w2.getId());
    }
}

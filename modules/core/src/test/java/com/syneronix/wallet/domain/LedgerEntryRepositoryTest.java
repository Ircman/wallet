package com.syneronix.wallet.domain;

import com.syneronix.wallet.common.*;
import com.syneronix.wallet.testing.BaseH2InMemoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerEntryRepositoryTest extends BaseH2InMemoryTest {

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void save_shouldPersistLedgerEntry() {
        WalletEntity wallet = new WalletEntity();
        wallet.setUserId(uuid());
        wallet.setCurrency(Currency.USD);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet = walletRepository.save(wallet);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setRequestId(uuid());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setAmount(BigDecimal.TEN);
        transaction.setCurrency(Currency.USD);
        transaction.setToWallet(wallet);
        transaction = transactionRepository.save(transaction);

        LedgerEntryEntity entry = new LedgerEntryEntity();
        entry.setTransaction(transaction);
        entry.setWallet(wallet);
        entry.setAmount(BigDecimal.TEN);
        entry.setDirection(MoneyFlowDirection.CREDIT);
        entry.setCurrency(Currency.USD);
        entry.setBalanceAfter(BigDecimal.TEN);

        LedgerEntryEntity saved = ledgerEntryRepository.save(entry);

        assertThat(saved.getId()).isNotNull();
    }
}

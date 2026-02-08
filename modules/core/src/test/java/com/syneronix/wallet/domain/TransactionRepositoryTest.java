package com.syneronix.wallet.domain;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.TransactionType;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.testing.BaseH2InMemoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRepositoryTest extends BaseH2InMemoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void save_shouldPersistTransaction() {
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

        TransactionEntity saved = transactionRepository.save(transaction);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRequestId()).isNotNull();
    }
}

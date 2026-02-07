package com.syneronix.wallet.services;

import com.syneronix.wallet.common.MoneyFlowDirection;
import com.syneronix.wallet.domain.LedgerEntryEntity;
import com.syneronix.wallet.domain.LedgerEntryRepository;
import com.syneronix.wallet.domain.TransactionEntity;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.testing.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

class LedgerServiceTest extends BaseUnitTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private LedgerService ledgerService;

    private TransactionEntity transaction;
    private WalletEntity wallet;

    @BeforeEach
    void setUp() {
        transaction = new TransactionEntity();
        wallet = new WalletEntity();
    }

    @Test
    void createLedgerEntry_shouldSaveEntity() {
        ledgerService.createLedgerEntry(transaction, MoneyFlowDirection.CREDIT, wallet, BigDecimal.TEN, BigDecimal.valueOf(100));

        ArgumentCaptor<LedgerEntryEntity> captor = ArgumentCaptor.forClass(LedgerEntryEntity.class);
        verify(ledgerEntryRepository).save(captor.capture());

        LedgerEntryEntity saved = captor.getValue();
        assertThat(saved.getTransaction()).isEqualTo(transaction);
        assertThat(saved.getWallet()).isEqualTo(wallet);
        assertThat(saved.getAmount()).isEqualTo(BigDecimal.TEN);
    }
}

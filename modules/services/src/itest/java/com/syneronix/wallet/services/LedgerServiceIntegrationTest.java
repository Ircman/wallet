package com.syneronix.wallet.services;

import com.syneronix.wallet.common.MoneyFlowDirection;
import com.syneronix.wallet.domain.TransactionEntity;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.testing.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.IllegalTransactionStateException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LedgerServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LedgerService ledgerService;

    @Test
    void createLedgerEntry_shouldThrowException_whenNoTransaction() {
        assertThrows(IllegalTransactionStateException.class, () ->
                ledgerService.createLedgerEntry(
                        new TransactionEntity(),
                        MoneyFlowDirection.CREDIT,
                        new WalletEntity(),
                        BigDecimal.TEN,
                        BigDecimal.TEN));
    }
}

package com.syneronix.wallet.services;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.TransactionType;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.*;
import com.syneronix.wallet.testing.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

class TransactionServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletRepository walletRepository;

    @SpyBean
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    private WalletEntity testWallet;

    private UUID requestId;

    @BeforeEach
    void setUp() {
        ledgerEntryRepository.deleteAll();
        transactionRepository.deleteAll();
        blacklistRepository.deleteAll();
        walletRepository.deleteAll();
        requestId = uuid();

        testWallet = new WalletEntity();
        testWallet.setUserId(uuid());
        testWallet.setCurrency(Currency.USD);
        testWallet.setBalance(BigDecimal.valueOf(100));
        testWallet.setStatus(WalletStatus.ACTIVE);
        testWallet = walletRepository.save(testWallet);
    }

    @Test
    void deposit_shouldIncreaseBalance_andCreateLedger() {
        BigDecimal amount = BigDecimal.valueOf(50);

        TransactionEntity transaction = transactionService.deposit(requestId, testWallet, amount, Currency.USD);

        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(transaction.getType()).isEqualTo(TransactionType.DEPOSIT);

        WalletEntity updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));

        assertThat(ledgerEntryRepository.count()).isEqualTo(1);
    }

    @Test
    void withdraw_shouldDecreaseBalance_andCreateLedger() {
        BigDecimal amount = BigDecimal.valueOf(50);

        TransactionEntity transaction = transactionService.withdraw(requestId, testWallet, amount, Currency.USD);

        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);

        WalletEntity updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(50));

        assertThat(ledgerEntryRepository.count()).isEqualTo(1);
    }

    @Test
    void withdraw_shouldFail_whenInsufficientFunds() {
        BigDecimal amount = BigDecimal.valueOf(200);

        TransactionEntity transaction = transactionService.withdraw(requestId, testWallet, amount, Currency.USD);

        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(transaction.getFailureReason()).isEqualTo("Insufficient funds");

        WalletEntity updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));

        assertThat(ledgerEntryRepository.count()).isEqualTo(0);
    }

    @Test
    void deposit_shouldRollbackLedger_whenExceptionOccursAfterLedgerCreation() {
        doAnswer(invocation -> {
            TransactionEntity t = invocation.getArgument(0);
            if (t.getStatus() == TransactionStatus.COMPLETED) {
                throw new RuntimeException("Simulated DB Error on Complete");
            }
            return invocation.callRealMethod();
        }).when(transactionRepository).save(any(TransactionEntity.class));

        BigDecimal amount = BigDecimal.valueOf(50);

        assertThrows(RuntimeException.class, () ->
                transactionService.deposit(requestId, testWallet, amount, Currency.USD)
        );

        assertThat(ledgerEntryRepository.count()).isEqualTo(0);

        WalletEntity updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }
}

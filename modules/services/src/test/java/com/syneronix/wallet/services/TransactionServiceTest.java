package com.syneronix.wallet.services;

import com.syneronix.wallet.api.errors.CurrencyMismatchException;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.MoneyFlowDirection;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.domain.TransactionEntity;
import com.syneronix.wallet.domain.TransactionRepository;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.domain.WalletRepository;
import com.syneronix.wallet.testing.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TransactionServiceTest extends BaseUnitTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private TransactionService transactionService;

    private UUID requestId;
    private WalletEntity wallet;
    private WalletEntity sender;
    private WalletEntity receiver;

    @BeforeEach
    void setUp() {
        requestId = uuid();

        wallet = new WalletEntity();
        wallet.setId(uuid());
        wallet.setCurrency(Currency.USD);
        wallet.setBalance(BigDecimal.valueOf(100));

        sender = new WalletEntity();
        sender.setId(uuid());
        sender.setCurrency(Currency.USD);
        sender.setBalance(BigDecimal.valueOf(100));

        receiver = new WalletEntity();
        receiver.setId(uuid());
        receiver.setCurrency(Currency.USD);
        receiver.setBalance(BigDecimal.ZERO);
    }

    @Test
    void deposit_shouldSucceed() {
        wallet.setBalance(BigDecimal.ZERO);
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArgument(0));

        TransactionEntity result = transactionService.deposit(requestId, wallet, BigDecimal.TEN, Currency.USD);

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.TEN);

        verify(walletRepository).save(wallet);
        verify(ledgerService).createLedgerEntry(eq(result), eq(MoneyFlowDirection.CREDIT), eq(wallet), eq(BigDecimal.TEN), eq(BigDecimal.TEN));
    }

    @Test
    void deposit_shouldThrow_whenCurrencyMismatch() {
        wallet.setCurrency(Currency.EUR);

        assertThrows(CurrencyMismatchException.class, () ->
                transactionService.deposit(requestId, wallet, BigDecimal.TEN, Currency.USD)
        );

        verifyNoInteractions(ledgerService);
    }

    @Test
    void withdraw_shouldSucceed() {
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArgument(0));

        TransactionEntity result = transactionService.withdraw(requestId, wallet, BigDecimal.TEN, Currency.USD);

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(90));

        verify(walletRepository).save(wallet);
        verify(ledgerService).createLedgerEntry(eq(result), eq(MoneyFlowDirection.DEBIT), eq(wallet), eq(BigDecimal.TEN), eq(BigDecimal.valueOf(90)));
    }

    @Test
    void withdraw_shouldFail_whenInsufficientFunds() {
        wallet.setBalance(BigDecimal.ZERO);
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArgument(0));

        TransactionEntity result = transactionService.withdraw(requestId, wallet, BigDecimal.TEN, Currency.USD);

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(result.getFailureReason()).isEqualTo("Insufficient funds");
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(walletRepository, never()).save(wallet);
        verifyNoInteractions(ledgerService);
    }

    @Test
    void transfer_shouldSucceed() {
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArgument(0));

        TransactionEntity result = transactionService.transfer(requestId, sender, receiver, BigDecimal.TEN, Currency.USD);

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(sender.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(90));
        assertThat(receiver.getBalance()).isEqualByComparingTo(BigDecimal.TEN);

        verify(walletRepository).save(sender);
        verify(walletRepository).save(receiver);

        verify(ledgerService).createLedgerEntry(eq(result), eq(MoneyFlowDirection.DEBIT), eq(sender), eq(BigDecimal.TEN), eq(BigDecimal.valueOf(90)));
        verify(ledgerService).createLedgerEntry(eq(result), eq(MoneyFlowDirection.CREDIT), eq(receiver), eq(BigDecimal.TEN), eq(BigDecimal.TEN));
    }
}

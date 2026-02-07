package com.syneronix.wallet.services;

import com.syneronix.wallet.api.errors.CurrencyMismatchException;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.MoneyFlowDirection;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.TransactionType;
import com.syneronix.wallet.domain.TransactionEntity;
import com.syneronix.wallet.domain.TransactionRepository;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.domain.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final LedgerService ledgerService;

    @Transactional
    public TransactionEntity deposit(UUID requestId, WalletEntity wallet, BigDecimal amount, Currency currency) {

        if (!wallet.getCurrency().equals(currency)) {
            throw new CurrencyMismatchException(wallet.getId(),wallet.getCurrency(), currency);
        }

        TransactionEntity transaction = createInitialTransaction(requestId, amount, currency, TransactionType.DEPOSIT);
        transaction.setToWallet(wallet);
        transaction = transactionRepository.save(transaction);

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        ledgerService.createLedgerEntry(transaction, MoneyFlowDirection.CREDIT,  wallet, amount, wallet.getBalance());

        transaction.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public TransactionEntity withdraw(UUID requestId, WalletEntity wallet, BigDecimal amount, Currency currency) {

        if (!wallet.getCurrency().equals(currency)) {
            throw new CurrencyMismatchException(wallet.getId(), wallet.getCurrency(), currency);
        }

        TransactionEntity transaction = createInitialTransaction(requestId, amount, currency, TransactionType.WITHDRAW);
        transaction.setFromWallet(wallet);
        transaction = transactionRepository.save(transaction);

        if (wallet.getBalance().compareTo(amount) < 0) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Insufficient funds");
            return transactionRepository.save(transaction);
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        ledgerService.createLedgerEntry(transaction, MoneyFlowDirection.DEBIT, wallet, amount, wallet.getBalance());

        transaction.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public TransactionEntity transfer(UUID requestId, WalletEntity fromWallet, WalletEntity toWallet, BigDecimal amount, Currency currency) {

        if (!fromWallet.getCurrency().equals(currency) || !toWallet.getCurrency().equals(currency)) {
            throw new CurrencyMismatchException(fromWallet.getId(),toWallet.getId(),toWallet.getCurrency(),fromWallet.getCurrency(), currency);
        }

        TransactionEntity transaction = createInitialTransaction(requestId, amount, currency, TransactionType.TRANSFER);
        transaction.setFromWallet(fromWallet);
        transaction.setToWallet(toWallet);
        transaction = transactionRepository.save(transaction);

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Insufficient funds");
            return transactionRepository.save(transaction);
        }

        // Update balances
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Create ledger entries (positive amounts)
        ledgerService.createLedgerEntry(transaction, MoneyFlowDirection.DEBIT, fromWallet, amount, fromWallet.getBalance());
        ledgerService.createLedgerEntry(transaction, MoneyFlowDirection.CREDIT, toWallet, amount, toWallet.getBalance());

        transaction.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionEntity> getTransactions(UUID walletId) {
        return transactionRepository.findAllByWalletId(walletId);
    }

    private TransactionEntity createInitialTransaction(UUID requestId, BigDecimal amount, Currency currency, TransactionType type) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setRequestId(requestId);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.PENDING);
        return transaction;
    }
}

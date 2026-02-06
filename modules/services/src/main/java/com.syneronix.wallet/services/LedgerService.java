package com.syneronix.wallet.services;

import com.syneronix.wallet.common.MoneyFlowDirection;
import com.syneronix.wallet.domain.LedgerEntryEntity;
import com.syneronix.wallet.domain.LedgerEntryRepository;
import com.syneronix.wallet.domain.TransactionEntity;
import com.syneronix.wallet.domain.WalletEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    //required existing transaction
    @Transactional(propagation = Propagation.MANDATORY)
    public void createLedgerEntry(TransactionEntity transaction, MoneyFlowDirection direction, WalletEntity wallet, BigDecimal amount, BigDecimal balanceAfter) {
        LedgerEntryEntity entry = new LedgerEntryEntity();
        entry.setTransaction(transaction);
        entry.setWallet(wallet);
        entry.setAmount(amount);
        entry.setDirection(direction);
        entry.setCurrency(wallet.getCurrency());
        entry.setBalanceAfter(balanceAfter);
        ledgerEntryRepository.save(entry);
        log.debug("Created ledger entry for transaction {} and wallet {}", transaction.getId(), wallet.getId());
    }
}

package com.syneronix.wallet.services;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.domain.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletEntity createWallet(UUID userId, Currency currency) {
        WalletEntity walletEntity = new WalletEntity();
        walletEntity.setUserId(userId);
        walletEntity.setCurrency(currency);
        return walletRepository.save(walletEntity);
    }

    @Transactional(readOnly = true)
    public Optional<WalletEntity> findByWalletIdReadOnly(UUID walletId) {
       return walletRepository.findById(walletId);
    }


    @Transactional()
    public Optional<WalletEntity> findWithLockingById(UUID walletId) {
        return walletRepository.findWithLockingById(walletId);
    }

    @Transactional
    public List<WalletEntity> lockAllByIdsAndCurrencyOrdered(List<UUID> ids, Currency currency) {
        return walletRepository.lockAllByIdsAndCurrencyOrdered(ids, currency);
    }
}

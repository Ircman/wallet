package com.syneronix.wallet.services;

import com.syneronix.wallet.api.errors.ConflictException;
import com.syneronix.wallet.api.errors.WalletNotFoundException;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.BlacklistEntity;
import com.syneronix.wallet.domain.BlacklistRepository;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.domain.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlacklistService {

    private final BlacklistRepository blacklistRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public void blockWallet(UUID walletId, String reason) {
        WalletEntity walletEntity = walletRepository.
                findWithLockingById(walletId).orElseThrow(() -> new WalletNotFoundException(walletId));

        if (blacklistRepository.existsByWalletId(walletId)) {
            throw new ConflictException("Wallet with ID %s is already blocked.".formatted(walletId.toString()));
        }

        BlacklistEntity entity = new BlacklistEntity();
        entity.setWalletId(walletId);
        entity.setReason(reason);
        blacklistRepository.save(entity);

        walletEntity.setStatus(WalletStatus.SUSPENDED);
        walletRepository.save(walletEntity);

        log.info("Wallet {} blocked. Reason: {}", walletId, reason);
    }

    @Transactional
    public void unblockWallet(UUID walletId) {
        boolean wasInBlacklist = blacklistRepository.findByWalletId(walletId)
                .map(entity -> {
                    blacklistRepository.delete(entity);
                    return true;
                }).orElse(false);

        if (!wasInBlacklist) {
            log.debug("Wallet ID {} was not in blacklist", walletId);
        }

        walletRepository.findWithLockingById(walletId).ifPresentOrElse(
                wallet -> {
                    wallet.setStatus(WalletStatus.ACTIVE);
                    walletRepository.save(wallet);
                    log.info("Wallet {} status updated to ACTIVE", walletId);
                },
                () -> log.info("Wallet {} does not exist in our database, only blacklist entry processed", walletId)
        );
        log.info("Unblock process completed for wallet ID {}", walletId);
    }


    @Transactional(readOnly = true)
    public boolean isBlocked(UUID walletId) {
        return blacklistRepository.existsByWalletId(walletId);
    }

    @Transactional(readOnly = true)
    public List<BlacklistEntity> getAllBlockedWallets() {
        return blacklistRepository.findAll();
    }


}

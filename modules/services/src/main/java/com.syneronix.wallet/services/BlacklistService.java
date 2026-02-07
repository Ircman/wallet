package com.syneronix.wallet.services;

import com.syneronix.wallet.api.errors.ConflictException;
import com.syneronix.wallet.api.errors.NotFoundException;
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
    public BlacklistEntity blockWallet(UUID walletId, String reason) {
        WalletEntity walletEntity = walletRepository.findWithLockingById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        if (blacklistRepository.existsByWalletId(walletId)) {
            throw new ConflictException("Wallet with ID %s is already blocked.".formatted(walletId.toString()));
        }

        BlacklistEntity entity = new BlacklistEntity();
        entity.setWalletId(walletId);
        entity.setReason(reason);
        BlacklistEntity savedEntity = blacklistRepository.save(entity);

        walletEntity.setStatus(WalletStatus.SUSPENDED);
        walletRepository.save(walletEntity);

        log.info("Wallet {} blocked. Reason: {}", walletId, reason);
        return savedEntity;
    }

    @Transactional
    public BlacklistEntity unblockWallet(UUID walletId) {
        BlacklistEntity entity = blacklistRepository.findByWalletId(walletId)
                .orElseThrow(() -> new NotFoundException("Wallet is not blocked: " + walletId));

        WalletEntity walletEntity = walletRepository.findWithLockingById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        blacklistRepository.delete(entity);

        walletEntity.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(walletEntity);

        log.info("Wallet {} unblocked and status set to ACTIVE", walletId);
        return entity;
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

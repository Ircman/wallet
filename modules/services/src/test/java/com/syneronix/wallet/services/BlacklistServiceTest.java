package com.syneronix.wallet.services;

import com.syneronix.wallet.api.errors.ConflictException;
import com.syneronix.wallet.api.errors.NotFoundException;
import com.syneronix.wallet.api.errors.WalletNotFoundException;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.BlacklistEntity;
import com.syneronix.wallet.domain.BlacklistRepository;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.domain.WalletRepository;
import com.syneronix.wallet.testing.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlacklistServiceTest extends BaseUnitTest {

    @Mock
    private BlacklistRepository blacklistRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private BlacklistService blacklistService;

    private UUID walletId;
    private WalletEntity wallet;
    private BlacklistEntity blacklistEntity;

    @BeforeEach
    void setUp() {
        walletId = uuid();
        wallet = new WalletEntity();
        wallet.setId(walletId);

        blacklistEntity = new BlacklistEntity();
        blacklistEntity.setWalletId(walletId);
    }

    @Test
    void blockWallet_shouldSucceed() {
        when(walletRepository.findWithLockingById(walletId)).thenReturn(Optional.of(wallet));
        when(blacklistRepository.existsByWalletId(walletId)).thenReturn(false);
        when(blacklistRepository.save(any(BlacklistEntity.class))).thenAnswer(i -> i.getArgument(0));

        BlacklistEntity result = blacklistService.blockWallet(walletId, "reason");

        assertThat(result.getWalletId()).isEqualTo(walletId);
        assertThat(result.getReason()).isEqualTo("reason");
        verify(walletRepository).save(wallet);
        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.SUSPENDED);
    }

    @Test
    void blockWallet_shouldThrow_whenWalletNotFound() {
        when(walletRepository.findWithLockingById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> blacklistService.blockWallet(walletId, "reason"));
    }

    @Test
    void blockWallet_shouldThrow_whenAlreadyBlocked() {
        when(walletRepository.findWithLockingById(walletId)).thenReturn(Optional.of(wallet));
        when(blacklistRepository.existsByWalletId(walletId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> blacklistService.blockWallet(walletId, "reason"));
    }

    @Test
    void unblockWallet_shouldSucceed() {
        when(blacklistRepository.findByWalletId(walletId)).thenReturn(Optional.of(blacklistEntity));
        when(walletRepository.findWithLockingById(walletId)).thenReturn(Optional.of(wallet));

        blacklistService.unblockWallet(walletId);

        verify(blacklistRepository).delete(blacklistEntity);
        verify(walletRepository).save(wallet);
        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
    }

    @Test
    void unblockWallet_shouldThrow_whenNotBlocked() {
        when(blacklistRepository.findByWalletId(walletId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> blacklistService.unblockWallet(walletId));
    }
}

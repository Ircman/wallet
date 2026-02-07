package com.syneronix.wallet.services;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.domain.WalletRepository;
import com.syneronix.wallet.testing.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletServiceTest extends BaseUnitTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private UUID userId;
    private UUID walletId;

    @BeforeEach
    void setUp() {
        userId = uuid();
        walletId = uuid();
    }

    @Test
    void createWallet_shouldSave() {
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        WalletEntity result = walletService.createWallet(userId, Currency.USD);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getCurrency()).isEqualTo(Currency.USD);
        verify(walletRepository).save(any());
    }

    @Test
    void findWithLockingById_shouldCallRepo() {
        when(walletRepository.findWithLockingById(walletId)).thenReturn(Optional.of(new WalletEntity()));

        walletService.findWithLockingById(walletId);

        verify(walletRepository).findWithLockingById(walletId);
    }

    @Test
    void lockAllByIdsAndCurrencyOrdered_shouldCallRepo() {
        List<UUID> ids = List.of(uuid(), uuid());
        walletService.lockAllByIdsAndCurrencyOrdered(ids, Currency.USD);
        verify(walletRepository).lockAllByIdsAndCurrencyOrdered(ids, Currency.USD);
    }
}

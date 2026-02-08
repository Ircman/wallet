package com.syneronix.wallet.domain;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.testing.BaseH2InMemoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class WalletRepositoryTest extends BaseH2InMemoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void save_shouldPersistWallet() {
        WalletEntity wallet = new WalletEntity();
        wallet.setUserId(uuid());
        wallet.setCurrency(Currency.USD);
        wallet.setBalance(BigDecimal.TEN);
        wallet.setStatus(WalletStatus.ACTIVE);

        WalletEntity saved = walletRepository.save(wallet);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void findById_shouldReturnWallet() {
        WalletEntity wallet = new WalletEntity();
        wallet.setUserId(uuid());
        wallet.setCurrency(Currency.USD);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet = walletRepository.save(wallet);

        Optional<WalletEntity> found = walletRepository.findById(wallet.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(wallet.getId());
    }
}

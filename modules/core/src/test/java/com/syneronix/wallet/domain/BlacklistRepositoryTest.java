package com.syneronix.wallet.domain;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.testing.BaseH2InMemoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BlacklistRepositoryTest extends BaseH2InMemoryTest {

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void save_shouldPersistBlacklist() {
        WalletEntity wallet = new WalletEntity();
        wallet.setUserId(uuid());
        wallet.setCurrency(Currency.USD);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet = walletRepository.save(wallet);

        BlacklistEntity entity = new BlacklistEntity();
        entity.setWalletId(wallet.getId());
        entity.setReason("Test");

        BlacklistEntity saved = blacklistRepository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(blacklistRepository.existsByWalletId(wallet.getId())).isTrue();
    }
}

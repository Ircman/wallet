package com.syneronix.wallet.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlacklistRepository extends JpaRepository<BlacklistEntity, UUID> {
    Optional<BlacklistEntity> findByWalletId(UUID walletId);
    boolean existsByWalletId(UUID walletId);
}

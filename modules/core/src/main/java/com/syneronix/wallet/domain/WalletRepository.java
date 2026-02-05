package com.syneronix.wallet.domain;

import com.syneronix.wallet.common.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {

    Optional<WalletEntity> findByUserIdAndCurrency(UUID userId, Currency currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletEntity> findWithLockingById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WalletEntity w where w.id in :ids and w.currency = :currency order by w.id")
    List<WalletEntity> lockAllByIdsAndCurrencyOrdered(@Param("ids") List<UUID> ids, @Param("currency") Currency currency);
}

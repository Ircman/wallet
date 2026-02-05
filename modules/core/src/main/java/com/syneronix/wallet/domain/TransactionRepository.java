package com.syneronix.wallet.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    Optional<TransactionEntity> findByRequestId(UUID requestId);

    @Query("SELECT t FROM TransactionEntity t WHERE t.fromWallet.id = :walletId OR t.toWallet.id = :walletId ORDER BY t.createdAt DESC")
    List<TransactionEntity> findAllByWalletId(@Param("walletId") UUID walletId);

    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.fromWallet.id = :walletId AND t.createdAt >= :since")
    long countTransactionsSince(@Param("walletId") UUID walletId, @Param("since") Instant since);
}

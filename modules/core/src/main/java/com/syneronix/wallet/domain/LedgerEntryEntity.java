package com.syneronix.wallet.domain;


import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.MoneyFlowDirection;
import com.syneronix.wallet.hibernate.base.BaseEntity;
import com.syneronix.wallet.hibernate.base.EntitySchema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "ledger_entries",
        schema = EntitySchema.NAME,
        indexes = {
                @Index(name = "idx_ledger_entries_transaction_id", columnList = "transaction_id"),
                @Index(name = "idx_ledger_entries_wallet_id", columnList = "wallet_id")
        }
)
@NoArgsConstructor
@Getter
@Setter
public class LedgerEntryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionEntity transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false, length = 6)
    @Enumerated(EnumType.STRING)
    private MoneyFlowDirection direction;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;
}

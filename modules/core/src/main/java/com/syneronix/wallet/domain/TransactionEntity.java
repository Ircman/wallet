package com.syneronix.wallet.domain;


import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.TransactionType;
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
        name = "transactions",
        schema = EntitySchema.NAME,
        indexes = {
                @Index(name = "idx_transactions_from_wallet", columnList = "from_wallet_id"),
                @Index(name = "idx_transactions_to_wallet", columnList = "to_wallet_id"),
                @Index(name = "idx_transactions_request_id", columnList = "request_id", unique = true)
        }
)
@NoArgsConstructor
@Getter
@Setter
public class TransactionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "request_id", nullable = false, unique = true)
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_id")
    private WalletEntity fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id")
    private WalletEntity toWallet;

    @Column(length = 1024)
    private String description;

    @Column(length = 1024)
    private String failureReason;
}

package com.syneronix.wallet.domain;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.hibernate.base.BaseEntity;
import com.syneronix.wallet.hibernate.base.EntitySchema;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "wallets",
        schema = EntitySchema.NAME,
        indexes = {
                    @Index(name = "idx_wallets_user_id", columnList = "user_id")
        }
)
@NoArgsConstructor
@Getter
@Setter
public class WalletEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WalletStatus status = WalletStatus.ACTIVE;

    @PositiveOrZero
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;
}

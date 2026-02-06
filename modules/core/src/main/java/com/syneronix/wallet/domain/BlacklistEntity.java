package com.syneronix.wallet.domain;

import com.syneronix.wallet.hibernate.base.BaseEntity;
import com.syneronix.wallet.hibernate.base.EntitySchema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "blacklist",
        schema = EntitySchema.NAME,
        indexes = {
                @Index(name = "idx_blacklist_wallet_id", columnList = "wallet_id", unique = true)
        }
)
@NoArgsConstructor
@Getter
@Setter
public class BlacklistEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "wallet_id", nullable = false, unique = true)
    private UUID walletId;

    @Column(nullable = false)
    private String reason;
}

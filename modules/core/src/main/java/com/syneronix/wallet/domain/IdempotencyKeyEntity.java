package com.syneronix.wallet.domain;


import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.RequestType;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.hibernate.base.BaseEntity;
import com.syneronix.wallet.hibernate.base.EntitySchema;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.UUID;

@Entity
@Table(
        name = "idempotency_keys",
        schema = EntitySchema.NAME,
        indexes = {
                @Index(name = "idx_idempotency_keys_request_id", columnList = "request_id", unique = true)
        }
)
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class IdempotencyKeyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "request_id", nullable = false, updatable = false, unique = true)
    private UUID requestId;

    @Column(name = "request_type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(updatable = false)
    private UUID fromWalletId;

    @Column(updatable = false)
    private UUID toWalletId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false)
    private int httpStatusCode = -1;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false , updatable = false)
    private String requestBody;

    @Column(length = 64)
    private String requestHash;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String responseBody;

    @Column
    private String failReason;
}

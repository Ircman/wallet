package com.syneronix.wallet.api.dto;

import com.syneronix.wallet.common.WalletStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Data
@Schema(description = "Response containing wallet details")
@EqualsAndHashCode(callSuper = true)
public class WalletResponse extends AbstractBaseResponse {
    @Schema(description = "Unique identifier of the wallet", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID id;

    @Schema(description = "ID of the user who owns the wallet", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "Currency of the wallet", example = "USD")
    private String currency;

    @Schema(description = "Current balance of the wallet", example = "1000.00")
    private BigDecimal balance = BigDecimal.ZERO;

    @Schema(description = "Status of the wallet (ACTIVE, INACTIVE, BLOCKED)", example = "ACTIVE", oneOf =  WalletStatus.class)
    private WalletStatus status = WalletStatus.PENDING;

    @Schema(description = "Timestamp when the wallet was created", example = "2023-10-27T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the wallet was last updated", example = "2023-10-27T12:00:00Z")
    private Instant updatedAt;
}

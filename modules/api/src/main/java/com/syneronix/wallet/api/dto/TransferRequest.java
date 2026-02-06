package com.syneronix.wallet.api.dto;

import com.syneronix.wallet.common.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Request to transfer funds between wallets")
public class TransferRequest extends AbstractBaseRequest {

    @NotNull
    @Schema(description = "ID of the source wallet", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID fromWalletId;

    @NotNull
    @Schema(description = "ID of the destination wallet", example = "b1ffcd00-0d1c-5fa9-cc7e-7cc0ce491b22", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID toWalletId;

    @NotNull
    @DecimalMin(value = "0.01")
    @Schema(description = "Amount to transfer", example = "25.50", minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotNull
    @Schema(description = "Currency of the transfer", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    private Currency currency;
}

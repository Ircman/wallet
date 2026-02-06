package com.syneronix.wallet.api.dto;

import com.syneronix.wallet.common.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Request to deposit funds into a wallet")
public class DepositRequest extends AbstractBaseRequest {

    @NotNull
    @DecimalMin(value = "0.01")
    @Schema(description = "Amount to deposit", example = "100.00", minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotNull
    @Schema(description = "Currency of the deposit", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    private Currency currency;
}

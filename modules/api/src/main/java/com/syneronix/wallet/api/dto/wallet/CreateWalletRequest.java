package com.syneronix.wallet.api.dto.wallet;

import com.syneronix.wallet.api.dto.AbstractBaseRequest;
import com.syneronix.wallet.common.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Request to create a new wallet")
public class CreateWalletRequest extends AbstractBaseRequest {

    @NotNull
    @Schema(description = "ID of the user who owns the wallet", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID userId;

    @NotNull
    @Schema(description = "Currency of the wallet", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    private Currency currency;
}

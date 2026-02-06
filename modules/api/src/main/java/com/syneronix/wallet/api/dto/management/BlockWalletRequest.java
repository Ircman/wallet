package com.syneronix.wallet.api.dto.management;

import com.syneronix.wallet.api.dto.AbstractBaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Request to block a wallet")
public class BlockWalletRequest extends AbstractBaseRequest {

    @NotNull
    @Schema(description = "ID of the wallet to block", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID walletId;

    @NotBlank
    @Schema(description = "Reason for blocking", example = "Suspicious activity", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}

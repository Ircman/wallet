package com.syneronix.wallet.api.dto.management;

import com.syneronix.wallet.common.WalletStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Response for unblocking a wallet")
public class UnblockWalletResponse {

    @Schema(description = "ID of the unblocked wallet", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID walletId;

    @Schema(description = "Status of the wallet after unblocking", example = "ACTIVE")
    private WalletStatus status;
}

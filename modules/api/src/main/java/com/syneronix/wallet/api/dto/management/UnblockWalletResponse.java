package com.syneronix.wallet.api.dto.management;

import com.syneronix.wallet.api.dto.AbstractBaseResponse;
import com.syneronix.wallet.common.WalletStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@Builder
@Schema(description = "Response for unblocking a wallet")
@EqualsAndHashCode(callSuper = true)
public class UnblockWalletResponse extends AbstractBaseResponse {

    @Schema(description = "ID of the unblocked wallet", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID walletId;

    @Schema(description = "Status of the wallet after unblocking", example = "ACTIVE")
    private WalletStatus status;
}

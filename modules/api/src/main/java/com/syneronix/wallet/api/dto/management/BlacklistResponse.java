package com.syneronix.wallet.api.dto.management;

import com.syneronix.wallet.api.dto.AbstractBaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;


@Data
@Builder
@Schema(description = "Details of a blocked wallet")
@EqualsAndHashCode(callSuper = true)
public class BlacklistResponse extends AbstractBaseResponse {

    @Schema(description = "ID of the blacklist entry", example = "b1ffcd00-0d1c-5fa9-cc7e-7cc0ce491b22")
    private UUID id;

    @Schema(description = "ID of the blocked wallet", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID walletId;

    @Schema(description = "Reason for blocking", example = "Suspicious activity")
    private String reason;

    @Schema(description = "Timestamp when the wallet was blocked", example = "2023-10-27T10:00:00Z")
    private Instant blockedAt;
}

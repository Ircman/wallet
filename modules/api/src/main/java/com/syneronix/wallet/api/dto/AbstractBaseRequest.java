package com.syneronix.wallet.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
@Schema(description = "Base request for all wallet operations")
public abstract class AbstractBaseRequest {

    @NotNull
    @Schema(description = "Unique request identifier for idempotency", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID requestId;

    @NotNull
    @Schema(description = "Timestamp of the request", example = "2023-10-27T10:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant timestamp;
}

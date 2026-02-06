package com.syneronix.wallet.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JsonNaming(SnakeCaseStrategy.class)
@Getter
@Setter
public abstract class AbstractBaseResponse {
    private UUID requestId;
}

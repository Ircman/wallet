package com.syneronix.wallet.api.errors;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(
        name = "ErrorResponse",
        description = "Standard API error response"
)
public class ErrorResponse {

    @Schema(
            description = "Short error code identifier",
            examples = {
                    "BAD_REQUEST",
                    "NOT_FOUND",
                    "CONFLICT",
                    "UNAUTHORIZED"
            }
    )
    private final String code;

    @Schema(
            description = "HTTP status code",
            example = "400"
    )
    private final int errorCode;

    @Schema(
            description = "Human-readable error message",
            example = "Detailed error message"
    )
    private final String message;

    @Schema(
            description = "Request path that caused the error",
            example = "/api/v1/path"
    )
    private final String path;

    public ErrorResponse(BaseApiExceptionModel ex, String path) {
        this.errorCode = ex.getStatus();
        this.code = ex.getError();
        this.message = ex.getMessage();
        this.path = path;
    }

    public ErrorResponse(
            String code,
            int errorCode,
            String message,
            String path
    ) {
        this.code = code;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
    }

    /* ============================================================
       INTERNAL
       ============================================================ */

}

package com.syneronix.wallet.api.errors;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorExamples {

    public static final String WALLET_NOT_FOUND_JSON = "{\"code\": \"NOT_FOUND\", \"errorCode\": 404, \"message\": \"Wallet with ID a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11 not found\", \"path\": \"/api/v1/wallets/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11\"}";
    public static final String INSUFFICIENT_FUNDS_JSON = "{\"code\": \"CONFLICT\", \"errorCode\": 409, \"message\": \"Insufficient funds\", \"path\": \"/api/v1/wallets/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/withdraw\"}";
    public static final String CURRENCY_MISMATCH_JSON = "{\"code\": \"BAD_REQUEST\", \"errorCode\": 400, \"message\": \"Currency mismatch: Wallet is USD but request is EUR\", \"path\": \"/api/v1/wallets/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/deposit\"}";
    public static final String TRANSACTION_FAILED_JSON = "{\"code\": \"UNPROCESSABLE_ENTITY\", \"errorCode\": 422, \"message\": \"Transaction failed: Insufficient funds\", \"path\": \"/api/v1/wallets/...\"}";
    public static final String VALIDATION_ERROR_JSON = "{\"code\": \"BAD_REQUEST\", \"description\": \"One or more validation errors occurred\", \"validationError\": [{\"field\": \"amount\", \"errorMessage\": \"must be greater than 0\"}]}";
    public static final String INTERNAL_SERVER_ERROR_JSON = "{\"code\": \"INTERNAL_SERVER_ERROR\", \"errorCode\": 500, \"message\": \"Unexpected server error\", \"path\": \"/api/v1/wallets/...\"}";
    public static final String REQUEST_TAMPERING_JSON = "{\"code\": \"BAD_REQUEST\", \"errorCode\": 400, \"message\": \"Invalid request\", \"path\": \"/api/v1/wallets/...\"}";
    public static final String WALLET_LOCKED_JSON = "{\"code\": \"LOCKED\", \"errorCode\": 423, \"message\": \"Wallet is locked\", \"path\": \"/api/v1/wallets/...\"}";
    public static final String RATE_LIMIT_EXCEEDED_JSON = "{\"code\": \"TOO_MANY_REQUESTS\", \"errorCode\": 429, \"message\": \"Rate limit exceeded\", \"path\": \"/api/v1/wallets/...\"}";

    @Schema(
            name = "WalletNotFoundResponse",
            description = "Example of 404 error",
            example = WALLET_NOT_FOUND_JSON
    )
    public static class WalletNotFound extends ErrorResponse {
        public WalletNotFound() {
            super("NOT_FOUND", 404, "Wallet with ID a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11 not found", "/api/v1/wallets/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        }

        @Override
        @Schema(example = "NOT_FOUND")
        public String getCode() {
            return super.getCode();
        }

        @Override
        @Schema(example = "404")
        public int getErrorCode() {
            return super.getErrorCode();
        }

        @Override
        @Schema(example = "Wallet with ID a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11 not found")
        public String getMessage() {
            return super.getMessage();
        }

        @Override
        @Schema(example = "/api/v1/wallets/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        public String getPath() {
            return super.getPath();
        }
    }

    @Schema(
            name = "TransactionFailedResponse",
            description = "Example of 422 error",
            example = TRANSACTION_FAILED_JSON
    )
    public static class TransactionFailed extends ErrorResponse {
        public TransactionFailed() {
            super("UNPROCESSABLE_ENTITY", 422, "Transaction failed: Insufficient funds", "/api/v1/wallets/...");
        }

        @Override
        @Schema(example = "UNPROCESSABLE_ENTITY")
        public String getCode() {
            return super.getCode();
        }

        @Override
        @Schema(example = "422")
        public int getErrorCode() {
            return super.getErrorCode();
        }

        @Override
        @Schema(example = "Transaction failed: Insufficient funds")
        public String getMessage() {
            return super.getMessage();
        }

        @Override
        @Schema(example = "/api/v1/wallets/...")
        public String getPath() {
            return super.getPath();
        }
    }


    @Schema(
            name = "InternalServerErrorResponse",
            description = "Example of 500 error",
            example = INTERNAL_SERVER_ERROR_JSON
    )
    public static class InternalServerError extends ErrorResponse {
        public InternalServerError() {
            super("INTERNAL_SERVER_ERROR", 500, "Unexpected server error", "/api/v1/wallets/...");
        }

        @Override
        @Schema(example = "INTERNAL_SERVER_ERROR")
        public String getCode() {
            return super.getCode();
        }

        @Override
        @Schema(example = "500")
        public int getErrorCode() {
            return super.getErrorCode();
        }

        @Override
        @Schema(example = "Unexpected server error")
        public String getMessage() {
            return super.getMessage();
        }

        @Override
        @Schema(example = "/api/v1/wallets/...")
        public String getPath() {
            return super.getPath();
        }
    }


    @Schema(
            name = "WalletLockedResponse",
            description = "Example of 423 error",
            example = WALLET_LOCKED_JSON
    )
    public static class WalletLocked extends ErrorResponse {
        public WalletLocked() {
            super("LOCKED", 423, "Wallet is locked", "/api/v1/wallets/...");
        }

        @Override
        @Schema(example = "LOCKED")
        public String getCode() {
            return super.getCode();
        }

        @Override
        @Schema(example = "423")
        public int getErrorCode() {
            return super.getErrorCode();
        }

        @Override
        @Schema(example = "Wallet is locked")
        public String getMessage() {
            return super.getMessage();
        }

        @Override
        @Schema(example = "/api/v1/wallets/...")
        public String getPath() {
            return super.getPath();
        }
    }

    @Schema(
            name = "RateLimitExceededResponse",
            description = "Example of 429 error",
            example = RATE_LIMIT_EXCEEDED_JSON
    )
    public static class RateLimitExceeded extends ErrorResponse {
        public RateLimitExceeded() {
            super("TOO_MANY_REQUESTS", 429, "Rate limit exceeded", "/api/v1/wallets/...");
        }

        @Override
        @Schema(example = "TOO_MANY_REQUESTS")
        public String getCode() {
            return super.getCode();
        }

        @Override
        @Schema(example = "429")
        public int getErrorCode() {
            return super.getErrorCode();
        }

        @Override
        @Schema(example = "Rate limit exceeded")
        public String getMessage() {
            return super.getMessage();
        }

        @Override
        @Schema(example = "/api/v1/wallets/...")
        public String getPath() {
            return super.getPath();
        }
    }
}

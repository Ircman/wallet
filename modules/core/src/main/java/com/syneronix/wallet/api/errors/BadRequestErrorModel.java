package com.syneronix.wallet.api.errors;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class BadRequestErrorModel {
    @Schema(description = "code", example = "BAD_REQUEST")
    final String code = "BAD_REQUEST";
    @Schema(example = "Field validation error")
    String description;
    @Schema(description = "list of validation errors")
    List<ValidationError> validationError;

    public BadRequestErrorModel(String description, List<ValidationError> validationError) {
        this.description = description;
        this.validationError = validationError;
    }

    public BadRequestErrorModel() {
        description = "One or more validation errors occurred";
        this.validationError = null;
    }

    @Getter
    @AllArgsConstructor
    public static class ValidationError {
        @Schema(description = "The name of the field where the error occurred", example = "registration_number")
        private String field;
        @Schema(description = "Error Description", example = "number must contain only digits")
        private String errorMessage;
    }
}

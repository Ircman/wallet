package com.syneronix.wallet.api.errors;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseApiExceptionModel {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

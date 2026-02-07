package com.syneronix.wallet.api.errors;

import org.springframework.http.HttpStatus;


public class BadRequestException extends BaseApiExceptionModel {
    public BadRequestException(String message) {
            super(HttpStatus.BAD_REQUEST, message);
    }
}

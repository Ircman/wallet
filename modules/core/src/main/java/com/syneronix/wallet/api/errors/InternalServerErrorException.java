package com.syneronix.wallet.api.errors;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends BaseApiExceptionModel {
    public InternalServerErrorException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR,message);
    }
}

package com.syneronix.wallet.api.errors;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseApiExceptionModel{
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND,message);
    }
}

package com.syneronix.wallet.api.errors;

import org.springframework.http.HttpStatus;

public class PreviousRequestFailedException extends BaseApiExceptionModel {
    public PreviousRequestFailedException(int httpStatus, String message) {
        super(HttpStatus.valueOf(httpStatus), message);
    }
}

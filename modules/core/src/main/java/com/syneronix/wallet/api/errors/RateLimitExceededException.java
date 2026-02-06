package com.syneronix.wallet.api.errors;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends BaseApiExceptionModel{
    public RateLimitExceededException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}

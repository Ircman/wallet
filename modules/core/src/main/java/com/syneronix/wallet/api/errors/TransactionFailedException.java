package com.syneronix.wallet.api.errors;

import org.springframework.http.HttpStatus;

public class TransactionFailedException extends BaseApiExceptionModel {
    public TransactionFailedException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}

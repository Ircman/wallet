package com.syneronix.wallet.api.errors;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class RequestTamperingException extends BaseApiExceptionModel {
    public RequestTamperingException(UUID requestId) {
            super(HttpStatus.CONFLICT, "Request body hash mismatch for key:"+requestId.toString());
    }
}

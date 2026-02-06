package com.syneronix.wallet.api.errors;

import org.springframework.http.HttpStatus;

public class WalletLockedException extends BaseApiExceptionModel{
    public WalletLockedException( String message) {
        super(HttpStatus.LOCKED, message);
    }
}

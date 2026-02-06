package com.syneronix.wallet.api.errors;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class WalletNotFoundException extends BaseApiExceptionModel {
    public WalletNotFoundException(UUID walletId) {
        super(HttpStatus.NOT_FOUND,"Wallet with id: %s not found".formatted(walletId));
    }
}

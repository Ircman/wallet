package com.syneronix.wallet.api.errors;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class WalletNotFoundException extends BaseApiExceptionModel {
    public WalletNotFoundException(UUID walletId) {
        super(HttpStatus.NOT_FOUND,"Wallet with id: %s not found".formatted(walletId));
    }

    public WalletNotFoundException(UUID walletFromId, UUID walletToId) {
        super(HttpStatus.NOT_FOUND, "Wallets with id: %s and %s not found".formatted(walletFromId, walletToId));
    }


}

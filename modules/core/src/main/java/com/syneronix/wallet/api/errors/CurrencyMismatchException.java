package com.syneronix.wallet.api.errors;


import com.syneronix.wallet.common.Currency;

import java.util.UUID;

public class CurrencyMismatchException extends BadRequestException{
    public CurrencyMismatchException(UUID walletId, Currency walletCurrency, Currency providedCurrency) {
        super("Operation rejected: Currency mismatch. [WalletID: %s | Wallet Currency: %s | Provided Currency: %s]".formatted(walletId, walletCurrency, providedCurrency));
    }

    public CurrencyMismatchException(UUID walletIdFrom, UUID walletIdTo, Currency walletFromCurrency,Currency walletToCurrency, Currency providedCurrency) {
        super("Operation rejected: Currency mismatch. [Wallet ID From: %s | Wallet ID To: %s | Wallet FROM Currency: %s | Wallet TO Currency: %s | Provided Currency: %s]"
                .formatted(walletIdFrom, walletIdTo, walletFromCurrency, walletToCurrency, providedCurrency));
    }
}

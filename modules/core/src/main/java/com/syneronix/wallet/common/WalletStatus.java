package com.syneronix.wallet.common;

public enum WalletStatus {
    PENDING,
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    CLOSED;

    public boolean isActive() {
        return this == ACTIVE;
    }
}

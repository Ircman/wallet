package com.syneronix.wallet.common;

public enum WalletStatus {
    PENDING,
    ACTIVE,
    INACTIVE,
    FROZEN,
    BLOCKED,
    CLOSED;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
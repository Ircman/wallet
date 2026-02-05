package com.syneronix.wallet.common;

import java.util.Arrays;

public enum MoneyFlowDirection {
    CREDIT,
    DEBIT;

    public static MoneyFlowDirection fromCode(String code) {
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown money flow direction code: " + code));
    }
}

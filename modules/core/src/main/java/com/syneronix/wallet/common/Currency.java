package com.syneronix.wallet.common;

import java.util.Arrays;

//ISO 4217
public enum Currency {
    USD,
    EUR,
    GBP;

    public static Currency fromCode(String code) {
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown currency code: " + code));
    }
}

package com.syneronix.wallet.testing;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface TestHelpers {


    default String uuidString() {
        return UUID.randomUUID().toString();
    }

    default UUID uuid() {
        return UUID.randomUUID();
    }

    default Instant instantNow() {
        return Instant.now();
    }

    default LocalDateTime now() {
        return LocalDateTime.now();
    }

    default LocalDate today() {
        return LocalDate.now();
    }
}

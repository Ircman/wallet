
package com.syneronix.wallet.spring;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DateTimeProvider {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    public LocalDate today() {
        return LocalDate.now();
    }

}

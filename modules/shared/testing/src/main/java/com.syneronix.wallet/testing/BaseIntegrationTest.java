package com.syneronix.wallet.testing;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Tag("integration")
@SpringBootTest(
        classes = IntegrationApplication.class,
        properties = "classpath=application-itest.yml",
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Slf4j
@ActiveProfiles(resolver = ItestActiveProfilesResolver.class)
public abstract class BaseIntegrationTest extends Assertions {
    protected static UUID uuid() {
        return UUID.randomUUID();
    }

    protected static LocalDateTime now() {
        return LocalDateTime.now();
    }

    protected static LocalDate today() {
        return LocalDate.now();
    }

}

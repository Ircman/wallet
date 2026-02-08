package com.syneronix.wallet.testing;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@EnableJpaAuditing
@ContextConfiguration(classes = IntegrationApplication.class)
@ActiveProfiles("test")
public abstract class BaseH2InMemoryTest implements TestHelpers {
}

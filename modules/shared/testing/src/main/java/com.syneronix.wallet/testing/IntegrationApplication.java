package com.syneronix.wallet.testing;

import static org.springframework.data.repository.config.BootstrapMode.LAZY;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("com.syneronix.wallet.*")
@EnableJpaRepositories(basePackages = "com.syneronix.wallet.*", bootstrapMode = LAZY)
@SpringBootApplication(scanBasePackages = "com.syneronix.perekup.auth")
public class IntegrationApplication {

}

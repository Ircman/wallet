package com.syneronix.wallet.testing;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.data.repository.config.BootstrapMode.LAZY;

@EntityScan("com.syneronix.wallet.*")
@EnableJpaRepositories(basePackages = "com.syneronix.wallet.*", bootstrapMode = LAZY)
@SpringBootApplication(scanBasePackages = "com.syneronix.wallet")
public class IntegrationApplication {

}

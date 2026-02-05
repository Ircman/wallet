package com.syneronix.wallet;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.syneronix.wallet")
public class WalletApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(WalletApplication.class).profiles("prd").run(args);
    }
}

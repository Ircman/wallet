package com.syneronix.wallet.config;


import com.syneronix.wallet.hibernate.base.EntitySchema;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class SyneronixFlywayConfiguration {

    @Value("${syneronix.flyway.locations:classpath:db/migration}")
    String[] migrationLocations;


    @Value("${syneronix.flyway.flyway-environment}")
    String flywayEnvironment;

    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true")
    @Bean(name = "flyway.syneronix", initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .outOfOrder(true)
                .locations(migrationLocations)
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .schemas(EntitySchema.NAME)
                .defaultSchema(EntitySchema.NAME)
                .placeholderReplacement(true)
                .placeholderPrefix("${")
                .placeholderSuffix("}")
                .placeholders(Map.of(
                        "flyway_environment", flywayEnvironment
                        )
                )
                .load();
    }

}

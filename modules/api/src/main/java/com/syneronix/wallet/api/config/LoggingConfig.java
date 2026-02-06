package com.syneronix.wallet.api.config;

import com.syneronix.wallet.api.LoggingFilter;
import com.syneronix.wallet.spring.EnvironmentProfiles;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

import java.util.EnumSet;

@Configuration
@Profile("!" + EnvironmentProfiles.INTEGRATION_TEST_PROFILE)
public class LoggingConfig {

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter() {
        FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LoggingFilter());
        registrationBean.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}

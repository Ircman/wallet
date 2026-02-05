package com.syneronix.wallet.testing;


import org.apache.commons.lang3.StringUtils;
import org.springframework.test.context.ActiveProfilesResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItestActiveProfilesResolver implements ActiveProfilesResolver {

    @Override
    public String[] resolve(Class<?> testClass) {
        String profiles = System.getProperty("spring.profiles.active");
        if (StringUtils.isNotEmpty(profiles)) {
            log.info("Active profiles selected: " + profiles);
            return profiles.split(",");
        }
        log.info("Active profiles selected: itest");
        return new String[]{"itest"};
    }

}

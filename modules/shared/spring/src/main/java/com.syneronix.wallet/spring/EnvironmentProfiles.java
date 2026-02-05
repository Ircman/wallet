package com.syneronix.wallet.spring;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@NoArgsConstructor(access = PRIVATE)
public class EnvironmentProfiles {

    public static final String PROD_PROFILE = "prd";

    public static final String INTEGRATION_TEST_PROFILE = "itest";

}

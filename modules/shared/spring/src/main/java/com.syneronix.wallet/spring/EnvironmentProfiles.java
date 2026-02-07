package com.syneronix.wallet.spring;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class EnvironmentProfiles {

    public static final String PROD_PROFILE = "prd";

    public static final String INTEGRATION_TEST_PROFILE = "itest";

}

package com.syneronix.wallet.testing;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class BaseUnitTest extends Assertions implements TestHelpers {
}

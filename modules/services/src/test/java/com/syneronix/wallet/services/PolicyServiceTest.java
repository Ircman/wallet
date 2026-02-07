package com.syneronix.wallet.services;

import com.syneronix.wallet.api.errors.RateLimitExceededException;
import com.syneronix.wallet.api.errors.WalletLockedException;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.config.PolicyProperties;
import com.syneronix.wallet.domain.TransactionRepository;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.testing.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PolicyServiceTest extends BaseUnitTest {

    @Mock
    private PolicyProperties policyProperties;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BlacklistService blacklistService;

    @InjectMocks
    private PolicyService policyService;

    private UUID walletId;
    private WalletEntity wallet;

    @BeforeEach
    void setUp() {
        walletId = uuid();
        wallet = new WalletEntity();
        wallet.setId(walletId);
        wallet.setStatus(WalletStatus.ACTIVE);

        PolicyProperties.RateLimit rateLimit = new PolicyProperties.RateLimit();
        rateLimit.setMaxTransactions(5);
        rateLimit.setWindowMinutes(1);
        when(policyProperties.getRateLimit()).thenReturn(rateLimit);
    }

    @Test
    void verifyRateLimit_shouldPass_whenUnderLimit() {
        when(transactionRepository.countTransactionsSince(any(), any())).thenReturn(4L);
        assertDoesNotThrow(() -> policyService.verifyRateLimit(walletId));
    }

    @Test
    void verifyRateLimit_shouldThrow_whenOverLimit() {
        when(transactionRepository.countTransactionsSince(any(), any())).thenReturn(5L);
        assertThrows(RateLimitExceededException.class, () -> policyService.verifyRateLimit(walletId));
    }

    @Test
    void verifyBlacklist_shouldPass_whenActiveAndNotBlocked() {
        when(blacklistService.isBlocked(any())).thenReturn(false);
        assertDoesNotThrow(() -> policyService.validate(wallet));
    }

    @Test
    void verifyBlacklist_shouldThrow_whenSuspended() {
        wallet.setStatus(WalletStatus.SUSPENDED);
        assertThrows(WalletLockedException.class, () -> policyService.validate(wallet));
    }

    @Test
    void verifyBlacklist_shouldThrow_whenBlocked() {
        when(blacklistService.isBlocked(any())).thenReturn(true);
        assertThrows(WalletLockedException.class, () -> policyService.validate(wallet));
    }
}

package com.syneronix.wallet.services;

import com.syneronix.wallet.api.errors.ConflictException;
import com.syneronix.wallet.api.errors.RateLimitExceededException;
import com.syneronix.wallet.api.errors.WalletLockedException;
import com.syneronix.wallet.config.PolicyProperties;
import com.syneronix.wallet.domain.TransactionRepository;
import com.syneronix.wallet.domain.WalletEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyProperties policyProperties;
    private final TransactionRepository transactionRepository;
    private final BlacklistService blacklistService;

    public void verifyRateLimit(UUID walletId) {
        int maxTransactions = policyProperties.getRateLimit().getMaxTransactions();
        int windowMinutes = policyProperties.getRateLimit().getWindowMinutes();

        Instant since = Instant.now().minus(windowMinutes, ChronoUnit.MINUTES);
        long count = transactionRepository.countTransactionsSince(walletId, since);

        if (count >= maxTransactions) {
            throw new RateLimitExceededException("Transaction limit exceeded: max " + maxTransactions + " per " + windowMinutes + " minute(s)");
        }
    }

     public void verifyBlacklist(WalletEntity walletEntity) {
        if (!walletEntity.getStatus().isActive()){
            throw new WalletLockedException("Operation denied: wallet is locked. Current status: %s."
                    .formatted(walletEntity.getStatus().name()));
        }
    }

    public void verifyBlacklist(UUID walletId) {
        if(blacklistService.isBlocked(walletId)){
            throw new WalletLockedException("Operation denied: wallet is suspended.");
        }
    }

    public void validate(WalletEntity walletEntity) {
        verifyRateLimit(walletEntity.getId());
        verifyBlacklist(walletEntity);
        verifyBlacklist(walletEntity.getId());
    }
}

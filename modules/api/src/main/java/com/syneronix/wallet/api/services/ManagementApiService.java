package com.syneronix.wallet.api.services;

import com.syneronix.wallet.api.dto.management.BlacklistResponse;
import com.syneronix.wallet.api.dto.management.ManagementMapper;
import com.syneronix.wallet.api.dto.management.UnblockWalletResponse;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.BlacklistEntity;
import com.syneronix.wallet.services.BlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagementApiService {

    private final BlacklistService blacklistService;
    private final ManagementMapper managementMapper;

    public BlacklistResponse blockWallet(UUID walletId, String reason) {
        BlacklistEntity entity = blacklistService.blockWallet(walletId, reason);
        return managementMapper.toBlacklistResponse(entity);
    }

    public UnblockWalletResponse unblockWallet(UUID walletId) {
        BlacklistEntity entity = blacklistService.unblockWallet(walletId);
        UnblockWalletResponse unblockResponse = managementMapper.toUnblockResponse(entity);
        unblockResponse.setStatus(WalletStatus.ACTIVE);
        return unblockResponse;
    }

    public List<BlacklistResponse> getAllBlockedWallets() {
        return blacklistService.getAllBlockedWallets().stream()
                .map(managementMapper::toBlacklistResponse)
                .collect(Collectors.toList());
    }
}

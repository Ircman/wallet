package com.syneronix.wallet.api.controllers.v1;

import com.syneronix.wallet.api.dto.management.BlockWalletRequest;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.BlacklistEntity;
import com.syneronix.wallet.domain.BlacklistRepository;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.domain.WalletRepository;
import com.syneronix.wallet.testing.BaseMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ManagementControllerTest extends BaseMockMvcTest {

    private static final String BASE_URL = "/api/v1/management/blacklist";

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    private WalletEntity testWallet;

    @BeforeEach
    void setUp() {
        blacklistRepository.deleteAll();
        walletRepository.deleteAll();

        testWallet = new WalletEntity();
        testWallet.setUserId(UUID.randomUUID());
        testWallet.setCurrency(Currency.USD);
        testWallet.setBalance(BigDecimal.ZERO);
        testWallet.setStatus(WalletStatus.ACTIVE);
        testWallet = walletRepository.save(testWallet);
    }

    @Test
    void blockWallet_shouldBlockExistingWalletTest() throws Exception {
        BlockWalletRequest request = createBlockWalletRequest(testWallet.getId());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallet_id").value(testWallet.getId().toString()))
                .andExpect(jsonPath("$.reason").value("Test block"));
    }

    @Test
    void blockWallet_shouldReturnNotFound_whenWalletDoesNotExistTest() throws Exception {
        BlockWalletRequest request = createBlockWalletRequest(uuid());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void blockWallet_shouldReturnConflict_whenWalletAlreadyBlockedTest() throws Exception {
        // Pre-block
        createBlacklistEntity(testWallet.getId(), "Already blocked");

        BlockWalletRequest request = createBlockWalletRequest(testWallet.getId());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void unblockWallet_shouldUnblockWalletTest() throws Exception {
        // Pre-block
        createBlacklistEntity(testWallet.getId(), "To be unblocked");

        mockMvc.perform(delete(BASE_URL + "/{id}", testWallet.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallet_id").value(testWallet.getId().toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void unblockWallet_shouldReturnNotFound_whenWalletNotBlockedTest() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", testWallet.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBlacklist_shouldReturnListOfBlockedWalletsTest() throws Exception {
        createBlacklistEntity(testWallet.getId(), "Listed");

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].wallet_id").value(testWallet.getId().toString()));
    }

    private BlockWalletRequest createBlockWalletRequest(UUID walletId) {
        BlockWalletRequest request = new BlockWalletRequest();
        request.setWalletId(walletId);
        request.setRequestId(uuid());
        request.setTimestamp(instantNow());
        request.setReason("Test block");
        return request;
    }

    private void createBlacklistEntity(UUID walletId, String reason) {
        BlacklistEntity blacklistEntity = new BlacklistEntity();
        blacklistEntity.setWalletId(walletId);
        blacklistEntity.setReason(reason);
        blacklistRepository.save(blacklistEntity);
    }
}

package com.syneronix.wallet.api.controllers.v1;

import com.syneronix.wallet.api.dto.wallet.CreateWalletRequest;
import com.syneronix.wallet.api.dto.wallet.DepositRequest;
import com.syneronix.wallet.api.dto.wallet.TransferRequest;
import com.syneronix.wallet.api.dto.wallet.WithdrawRequest;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.TransactionType;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.*;
import com.syneronix.wallet.testing.BaseMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WalletControllerTest extends BaseMockMvcTest {

    private static final String BASE_URL = "/api/v1/wallets";

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    private WalletEntity testWallet;

    @BeforeEach
    void setUp() {
        idempotencyKeyRepository.deleteAll();
        ledgerEntryRepository.deleteAll();
        transactionRepository.deleteAll();
        blacklistRepository.deleteAll();
        walletRepository.deleteAll();
        testWallet = createWallet(BigDecimal.valueOf(100));
    }

    // --- Create Wallet ---

    @Test
    void createWallet_shouldCreateNewWallet() throws Exception {
        CreateWalletRequest request = createCreateWalletRequest(uuid(), Currency.USD);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_id").value(request.getUserId().toString()))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void createWallet_shouldReturnBadRequest_whenCurrencyIsMissing() throws Exception {
        CreateWalletRequest request = createCreateWalletRequest(uuid(), null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());
    }

    // --- Get Wallet ---

    @Test
    void getWallet_shouldReturnWallet() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", testWallet.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testWallet.getId().toString()))
                .andExpect(jsonPath("$.balance").value(100));
    }

    @Test
    void getWallet_shouldReturnNotFound_whenWalletDoesNotExist() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", uuid()))
                .andExpect(status().isNotFound());
    }

    // --- Deposit ---

    @Test
    void deposit_shouldAddFunds() throws Exception {
        DepositRequest request = createDepositRequest(BigDecimal.valueOf(50), Currency.USD);

        mockMvc.perform(post(BASE_URL + "/{id}/deposit", testWallet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(50))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Verify balance updated
        mockMvc.perform(get(BASE_URL + "/{id}", testWallet.getId()))
                .andExpect(jsonPath("$.balance").value(150.0));
    }

    @Test
    void deposit_shouldReturnBadRequest_whenCurrencyMismatch() throws Exception {
        DepositRequest request = createDepositRequest(BigDecimal.valueOf(50), Currency.EUR);

        mockMvc.perform(post(BASE_URL + "/{id}/deposit", testWallet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deposit_shouldReturnOk_whenIdempotentRequest() throws Exception {
        DepositRequest request = createDepositRequest(BigDecimal.valueOf(50), Currency.USD);

        // First request
        mockMvc.perform(post(BASE_URL + "/{id}/deposit", testWallet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk());

        // Second request (same ID)
        mockMvc.perform(post(BASE_URL + "/{id}/deposit", testWallet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk());

        // Verify balance added ONLY ONCE
        mockMvc.perform(get(BASE_URL + "/{id}", testWallet.getId()))
                .andExpect(jsonPath("$.balance").value(150.0));
    }

    @Test
    void deposit_shouldReturnLocked_whenWalletIsBlacklisted() throws Exception {
        blacklistWallet(testWallet.getId());

        DepositRequest request = createDepositRequest(BigDecimal.valueOf(50), Currency.USD);

        mockMvc.perform(post(BASE_URL + "/{id}/deposit", testWallet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isLocked());
    }

    // --- Withdraw ---

    @Test
    void withdraw_shouldDeductFunds() throws Exception {
        WithdrawRequest request = createWithdrawRequest(BigDecimal.valueOf(50));

        mockMvc.perform(post(BASE_URL + "/{id}/withdraw", testWallet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get(BASE_URL + "/{id}", testWallet.getId()))
                .andExpect(jsonPath("$.balance").value(50.0));
    }

    @Test
    void withdraw_shouldReturnUnprocessableEntity_whenInsufficientFunds() throws Exception {
        WithdrawRequest request = createWithdrawRequest(BigDecimal.valueOf(200));

        mockMvc.perform(post(BASE_URL + "/{id}/withdraw", testWallet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableEntity()); // 422
    }

    @Test
    void withdraw_shouldReturnLocked_whenWalletIsBlacklisted() throws Exception {
        blacklistWallet(testWallet.getId());

        WithdrawRequest request = createWithdrawRequest(BigDecimal.valueOf(50));

        mockMvc.perform(post(BASE_URL + "/{id}/withdraw", testWallet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isLocked());
    }

    // --- Transfer ---

    @Test
    void transfer_shouldTransferFunds() throws Exception {
        WalletEntity sender = createWallet(BigDecimal.valueOf(100));
        WalletEntity receiver = createWallet(BigDecimal.ZERO);

        TransferRequest request = createTransferRequest(sender.getId(), receiver.getId(), BigDecimal.valueOf(50));

        mockMvc.perform(post(BASE_URL + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Verify balances
        mockMvc.perform(get(BASE_URL + "/{id}", sender.getId()))
                .andExpect(jsonPath("$.balance").value(50.0));

        mockMvc.perform(get(BASE_URL + "/{id}", receiver.getId()))
                .andExpect(jsonPath("$.balance").value(50.0));
    }

    @Test
    void transfer_shouldReturnUnprocessableEntity_whenInsufficientFunds() throws Exception {
        WalletEntity sender = createWallet(BigDecimal.valueOf(100));
        WalletEntity receiver = createWallet(BigDecimal.ZERO);

        TransferRequest request = createTransferRequest(sender.getId(), receiver.getId(), BigDecimal.valueOf(200));

        mockMvc.perform(post(BASE_URL + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void transfer_shouldReturnLocked_whenSenderIsBlacklisted() throws Exception {
        WalletEntity sender = createWallet(BigDecimal.valueOf(100));
        WalletEntity receiver = createWallet(BigDecimal.ZERO);
        blacklistWallet(sender.getId());

        TransferRequest request = createTransferRequest(sender.getId(), receiver.getId(), BigDecimal.valueOf(50));

        mockMvc.perform(post(BASE_URL + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isLocked());
    }

    @Test
    void transfer_shouldReturnLocked_whenReceiverIsBlacklisted() throws Exception {
        WalletEntity sender = createWallet(BigDecimal.valueOf(100));
        WalletEntity receiver = createWallet(BigDecimal.ZERO);
        blacklistWallet(receiver.getId());

        TransferRequest request = createTransferRequest(sender.getId(), receiver.getId(), BigDecimal.valueOf(50));

        mockMvc.perform(post(BASE_URL + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isLocked());
    }

    // --- Get Transactions ---

    @Test
    void getTransactions_shouldReturnList() throws Exception {
        createTransaction(testWallet, TransactionType.DEPOSIT, BigDecimal.valueOf(100));
        createTransaction(testWallet, TransactionType.WITHDRAW, BigDecimal.valueOf(50));

        mockMvc.perform(get(BASE_URL + "/{id}/transactions", testWallet.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // --- Helpers ---

    private WalletEntity createWallet(BigDecimal balance) {
        WalletEntity walletEntity = new WalletEntity();
        walletEntity.setUserId(uuid());
        walletEntity.setCurrency(Currency.USD);
        walletEntity.setBalance(balance);
        walletEntity.setStatus(WalletStatus.ACTIVE);
        return walletRepository.save(walletEntity);
    }

    private void createTransaction(WalletEntity wallet, TransactionType type, BigDecimal amount) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setRequestId(uuid());
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setAmount(amount);
        transaction.setCurrency(wallet.getCurrency());
        if (type == TransactionType.DEPOSIT) {
            transaction.setToWallet(wallet);
        } else {
            transaction.setFromWallet(wallet);
        }
        transactionRepository.save(transaction);
    }

    private void blacklistWallet(UUID walletId) {
        BlacklistEntity blacklistEntity = new BlacklistEntity();
        blacklistEntity.setWalletId(walletId);
        blacklistEntity.setReason("Test blacklist");
        blacklistRepository.save(blacklistEntity);

        walletRepository.findById(walletId).ifPresent(w -> {
            w.setStatus(WalletStatus.SUSPENDED);
            walletRepository.save(w);
        });
    }

    private CreateWalletRequest createCreateWalletRequest(UUID userId, Currency currency) {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(userId);
        request.setCurrency(currency);
        request.setRequestId(uuid());
        request.setTimestamp(Instant.now());
        return request;
    }

    private DepositRequest createDepositRequest(BigDecimal amount, Currency currency) {
        DepositRequest request = new DepositRequest();
        request.setAmount(amount);
        request.setCurrency(currency);
        request.setRequestId(uuid());
        request.setTimestamp(Instant.now());
        return request;
    }

    private WithdrawRequest createWithdrawRequest(BigDecimal amount) {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(amount);
        request.setCurrency(Currency.USD);
        request.setRequestId(uuid());
        request.setTimestamp(Instant.now());
        return request;
    }

    private TransferRequest createTransferRequest(UUID from, UUID to, BigDecimal amount) {
        TransferRequest request = new TransferRequest();
        request.setFromWalletId(from);
        request.setToWalletId(to);
        request.setAmount(amount);
        request.setCurrency(Currency.USD);
        request.setRequestId(uuid());
        request.setTimestamp(Instant.now());
        return request;
    }
}

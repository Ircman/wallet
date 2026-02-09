package com.syneronix.wallet.api.dto.wallet;

import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.TransactionType;
import com.syneronix.wallet.common.WalletStatus;
import com.syneronix.wallet.domain.TransactionEntity;
import com.syneronix.wallet.domain.WalletEntity;
import com.syneronix.wallet.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;


class WalletMapperTest extends BaseUnitTest {

    private final WalletMapper mapper = Mappers.getMapper(WalletMapper.class);

    @Test
    void entityToResponse_shouldMapCorrectly() {
        WalletEntity entity = new WalletEntity();
        entity.setId(uuid());
        entity.setUserId(uuid());
        entity.setCurrency(Currency.USD);
        entity.setBalance(BigDecimal.valueOf(100.50));
        entity.setStatus(WalletStatus.ACTIVE);
        entity.setCreatedAt(instantNow());
        entity.setUpdatedAt(instantNow().plusSeconds(60));
        entity.setVersion(1L);

        WalletResponse response = mapper.entityToResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(entity.getId());
        assertThat(response.getUserId()).isEqualTo(entity.getUserId());
        assertThat(response.getCurrency()).isEqualTo(entity.getCurrency().name());
        assertThat(response.getBalance()).isEqualTo(entity.getBalance());
        assertThat(response.getStatus()).isEqualTo(entity.getStatus());
        assertThat(response.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    void toWalletCreateResponse_fromRequest_shouldMapCorrectly() {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(uuid());
        request.setCurrency(Currency.EUR);
        request.setRequestId(uuid());

        WalletResponse response = mapper.toWalletCreateResponse(request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(request.getUserId());
        assertThat(response.getCurrency()).isEqualTo(request.getCurrency().name());
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
    }

    @Test
    void toWalletCreateResponse_fromEntityAndRequest_shouldMapCorrectly() {
        WalletEntity entity = new WalletEntity();
        entity.setId(uuid());
        entity.setBalance(BigDecimal.ZERO);
        entity.setStatus(WalletStatus.ACTIVE);
        entity.setCreatedAt(instantNow());
        entity.setUpdatedAt(instantNow());

        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(uuid());
        request.setCurrency(Currency.GBP);
        request.setRequestId(uuid());

        WalletResponse response = mapper.toWalletCreateResponse(entity, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(entity.getId());
        assertThat(response.getBalance()).isEqualTo(entity.getBalance());
        assertThat(response.getStatus()).isEqualTo(entity.getStatus());
        assertThat(response.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());

        assertThat(response.getUserId()).isEqualTo(request.getUserId());
        assertThat(response.getCurrency()).isEqualTo(request.getCurrency().name());
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
    }

    @Test
    void toTransactionResponse_fromEntity_shouldMapCorrectly() {
        WalletEntity fromWallet = new WalletEntity();
        fromWallet.setId(uuid());

        WalletEntity toWallet = new WalletEntity();
        toWallet.setId(uuid());

        TransactionEntity entity = new TransactionEntity();
        entity.setId(uuid());
        entity.setRequestId(uuid());
        entity.setType(TransactionType.TRANSFER);
        entity.setStatus(TransactionStatus.COMPLETED);
        entity.setAmount(BigDecimal.valueOf(50.00));
        entity.setCurrency(Currency.USD);
        entity.setFromWallet(fromWallet);
        entity.setToWallet(toWallet);
        entity.setCreatedAt(instantNow());
        entity.setFailureReason("None");

        TransactionResponse response = mapper.toTransactionResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(entity.getId());
        assertThat(response.getType()).isEqualTo(entity.getType());
        assertThat(response.getStatus()).isEqualTo(entity.getStatus());
        assertThat(response.getAmount()).isEqualTo(entity.getAmount());
        assertThat(response.getCurrency()).isEqualTo(entity.getCurrency());
        assertThat(response.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(response.getFailureReason()).isEqualTo(entity.getFailureReason());
        assertThat(response.getFromWalletId()).isEqualTo(fromWallet.getId());
        assertThat(response.getToWalletId()).isEqualTo(toWallet.getId());
    }

    @Test
    void toTransactionResponse_fromDepositRequest_shouldMapCorrectly() {
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setCurrency(Currency.USD);
        request.setRequestId(uuid());

        TransactionResponse response = mapper.toTransactionResponse(request);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualTo(request.getAmount());
        assertThat(response.getCurrency()).isEqualTo(request.getCurrency());
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
    }

    @Test
    void toTransactionResponse_fromTransferRequest_shouldMapCorrectly() {
        TransferRequest request = new TransferRequest();
        request.setFromWalletId(uuid());
        request.setToWalletId(uuid());
        request.setAmount(BigDecimal.valueOf(200));
        request.setCurrency(Currency.EUR);
        request.setRequestId(uuid());

        TransactionResponse response = mapper.toTransactionResponse(request);

        assertThat(response).isNotNull();
        assertThat(response.getFromWalletId()).isEqualTo(request.getFromWalletId());
        assertThat(response.getToWalletId()).isEqualTo(request.getToWalletId());
        assertThat(response.getAmount()).isEqualTo(request.getAmount());
        assertThat(response.getCurrency()).isEqualTo(request.getCurrency());
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
    }
}

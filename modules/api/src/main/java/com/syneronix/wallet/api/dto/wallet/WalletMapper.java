package com.syneronix.wallet.api.dto.wallet;

import com.syneronix.wallet.domain.TransactionEntity;
import com.syneronix.wallet.domain.WalletEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletMapper {

    WalletResponse entityToResponse(WalletEntity entity);

    WalletResponse toWalletCreateResponse(CreateWalletRequest request);

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "userId", source = "request.userId")
    @Mapping(target = "currency", source = "request.currency")
    @Mapping(target = "balance", source = "entity.balance")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "updatedAt", source = "entity.updatedAt")
    @Mapping(target = "status", source = "entity.status")
    @Mapping(target = "requestId", source = "request.requestId")
    WalletResponse toWalletCreateResponse(WalletEntity entity, CreateWalletRequest request);


    TransactionResponse toTransactionResponse(TransactionEntity entity);

    TransactionResponse toTransactionResponse(DepositRequest request);

    TransactionResponse toTransactionResponse(TransferRequest request);
}

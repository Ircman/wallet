package com.syneronix.wallet.api.dto.management;

import com.syneronix.wallet.domain.BlacklistEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ManagementMapper {

    @Mapping(target = "blockedAt", source = "createdAt")
    BlacklistResponse toBlacklistResponse(BlacklistEntity entity);

    UnblockWalletResponse toUnblockResponse(BlacklistEntity entity);
}

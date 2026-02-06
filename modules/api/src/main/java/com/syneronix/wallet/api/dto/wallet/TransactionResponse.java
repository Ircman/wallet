package com.syneronix.wallet.api.dto.wallet;

import com.syneronix.wallet.api.dto.AbstractBaseResponse;
import com.syneronix.wallet.common.Currency;
import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Schema(description = "Response containing transaction details")
@EqualsAndHashCode(callSuper = true)
public class TransactionResponse extends AbstractBaseResponse {
    @Schema(description = "Unique identifier of the transaction", example = "24e6e87b-2767-40ff-83c4-79fe4df0859a")
    private UUID id;

    @Schema(description = "Type of the transaction", example = "DEPOSIT", oneOf = TransactionType.class)
    private TransactionType type;

    @Schema(description = "Status of the transaction", example = "COMPLETED", oneOf =  TransactionStatus.class)
    private TransactionStatus status;

    @Schema(description = "Amount of the transaction", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Currency of the transaction", example = "USD", oneOf = Currency.class)
    private Currency currency;

    @Schema(description = "ID of the source wallet (if applicable)", example = "3e28f430-269e-4def-a162-f621148dc7e7")
    private UUID fromWalletId;

    @Schema(description = "ID of the destination wallet (if applicable)", example = "cace0df3-5878-4763-a200-8372dad6fbf2")
    private UUID toWalletId;

    @Schema(description = "Timestamp when the transaction was created", example = "2023-10-27T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Reason for failure (if status is FAILED)", example = "Insufficient funds")
    private String failureReason;
}

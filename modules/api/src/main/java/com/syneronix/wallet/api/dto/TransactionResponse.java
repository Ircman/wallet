package com.syneronix.wallet.api.dto;

import com.syneronix.wallet.common.TransactionStatus;
import com.syneronix.wallet.common.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Schema(description = "Response containing transaction details")
public class TransactionResponse {
    @Schema(description = "Unique identifier of the transaction", example = "24e6e87b-2767-40ff-83c4-79fe4df0859a")
    private UUID id;

    @Schema(description = "Original request ID for idempotency", example = "f09f21ae-2107-4696-ac8c-7af7ed572ef9")
    private String requestId;

    @Schema(description = "Type of the transaction", example = "DEPOSIT")
    private TransactionType type;

    @Schema(description = "Status of the transaction", example = "COMPLETED", oneOf =  TransactionStatus.class)
    private TransactionStatus status;

    @Schema(description = "Amount of the transaction", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Currency of the transaction", example = "USD")
    private String currency;

    @Schema(description = "ID of the source wallet (if applicable)", example = "3e28f430-269e-4def-a162-f621148dc7e7")
    private UUID fromWalletId;

    @Schema(description = "ID of the destination wallet (if applicable)", example = "cace0df3-5878-4763-a200-8372dad6fbf2")
    private UUID toWalletId;

    @Schema(description = "Timestamp when the transaction was created", example = "2023-10-27T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Reason for failure (if status is FAILED)", example = "Insufficient funds")
    private String failureReason;
}

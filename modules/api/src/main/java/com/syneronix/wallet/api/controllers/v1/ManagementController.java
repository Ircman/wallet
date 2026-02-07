package com.syneronix.wallet.api.controllers.v1;


import com.syneronix.wallet.api.dto.management.BlacklistResponse;
import com.syneronix.wallet.api.dto.management.BlockWalletRequest;
import com.syneronix.wallet.api.dto.management.UnblockWalletResponse;
import com.syneronix.wallet.api.errors.BadRequestErrorModel;
import com.syneronix.wallet.api.errors.ErrorExamples;
import com.syneronix.wallet.api.errors.ErrorResponse;
import com.syneronix.wallet.api.services.ManagementApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.syneronix.wallet.api.controllers.ApiVersion.V1;

@RestController
@RequestMapping(value = V1 + "/management", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Management", description = "Administrative operations for wallet management")
public class ManagementController {

    private final ManagementApiService managementApiService;

    @Operation(summary = "Block a wallet", description = "Adds a wallet to the blacklist, preventing further transactions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Wallet blocked successfully",
                            content = @Content(schema = @Schema(implementation = BlacklistResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestErrorModel.class))),
                    @ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content(schema = @Schema(implementation = ErrorExamples.WalletNotFound.class))),
                    @ApiResponse(responseCode = "409", description = "Wallet already blocked", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorExamples.InternalServerError.class)))
            })
    @PostMapping(value = "/blacklist", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BlacklistResponse> blockWallet(@Valid @RequestBody BlockWalletRequest request) {
        return ResponseEntity.ok(managementApiService.blockWallet(request.getWalletId(), request.getReason()));
    }

    @Operation(summary = "Unblock a wallet", description = "Removes a wallet from the blacklist.",
            parameters = {
                    @Parameter(name = "walletId", description = "ID of the wallet to unblock", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Wallet unblocked successfully",
                            content = @Content(schema = @Schema(implementation = UnblockWalletResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestErrorModel.class))),
                    @ApiResponse(responseCode = "404", description = "Wallet not found or not blocked", content = @Content(schema = @Schema(implementation = ErrorExamples.WalletNotFound.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorExamples.InternalServerError.class)))
            })
    @DeleteMapping("/blacklist/{walletId}")
    public ResponseEntity<UnblockWalletResponse> unblockWallet(@PathVariable UUID walletId) {
        return ResponseEntity.ok(managementApiService.unblockWallet(walletId));
    }

    @Operation(summary = "Get blacklist", description = "Retrieves a list of all blocked wallets.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List retrieved successfully",
                            content = @Content(schema = @Schema(implementation = BlacklistResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorExamples.InternalServerError.class)))
            })
    @GetMapping("/blacklist")
    public ResponseEntity<List<BlacklistResponse>> getBlacklist() {
        return ResponseEntity.ok(managementApiService.getAllBlockedWallets());
    }
}

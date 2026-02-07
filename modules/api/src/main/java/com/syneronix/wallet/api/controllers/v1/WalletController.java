package com.syneronix.wallet.api.controllers.v1;


import com.syneronix.wallet.api.dto.wallet.*;
import com.syneronix.wallet.api.errors.BadRequestErrorModel;
import com.syneronix.wallet.api.errors.ErrorExamples;
import com.syneronix.wallet.api.errors.ErrorResponse;
import com.syneronix.wallet.api.services.WalletApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static com.syneronix.wallet.api.controllers.ApiVersion.V1;

@RestController
@RequestMapping(value = V1 + "/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Operations related to wallet management and transactions")
public class WalletController {

    private final WalletApiService walletApiService;

    @Operation(summary = "Create a new wallet", description = "Creates a new wallet for a user with a specific currency. This operation is idempotent.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Wallet created successfully",
                            content = @Content(schema = @Schema(implementation = WalletResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestErrorModel.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorExamples.InternalServerError.class)))
            })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        WalletResponse response = walletApiService.createWallet(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/wallets/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get wallet details", description = "Retrieves details of a specific wallet by ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the wallet to retrieve",
                            example = "cfb87bfc-6e9d-407e-8cc9-9f11e48bd390", in = ParameterIn.PATH, required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Wallet found",
                            content = @Content(schema = @Schema(implementation = WalletResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content(schema = @Schema(implementation = ErrorExamples.WalletNotFound.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorExamples.InternalServerError.class)))
            })
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID id) {
        return ResponseEntity.ok(walletApiService.getWallet(id));
    }

    @Operation(summary = "Deposit funds", description = "Deposits funds into a specific wallet. This operation is idempotent.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the wallet to deposit to",
                            example = "cfb87bfc-6e9d-407e-8cc9-9f11e48bd390", in = ParameterIn.PATH, required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deposit successful",
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(oneOf = {ErrorResponse.class, BadRequestErrorModel.class}),
                                    examples = {
                                            @ExampleObject(name = "Validation Error", ref = "#/components/examples/ValidationError"),
                                            @ExampleObject(name = "Currency Mismatch", ref = "#/components/examples/CurrencyMismatch")
                                    }
                            )),
                    @ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content(schema = @Schema(implementation = ErrorExamples.WalletNotFound.class))),
                    @ApiResponse(responseCode = "422", description = "Unprocessable Entity (insufficient funds)", content = @Content(schema = @Schema(implementation = ErrorExamples.TransactionFailed.class))),
                    @ApiResponse(responseCode = "423", description = "Wallet locked (SUSPENDED)", content = @Content(schema = @Schema(implementation = ErrorExamples.WalletLocked.class))),
                    @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(schema = @Schema(implementation = ErrorExamples.RateLimitExceeded.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorExamples.InternalServerError.class)))
            })
    @PostMapping(value = "/{id}/deposit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionResponse> deposit(@PathVariable UUID id, @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(walletApiService.deposit(id, request));
    }

    @Operation(summary = "Withdraw funds", description = "Withdraws funds from a specific wallet. Checks balance and limits. This operation is idempotent.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the wallet to withdraw from",
                            example = "cfb87bfc-6e9d-407e-8cc9-9f11e48bd390", in = ParameterIn.PATH, required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Withdrawal successful",
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(oneOf = {ErrorResponse.class, BadRequestErrorModel.class}),
                                    examples = {
                                            @ExampleObject(name = "Validation Error", ref = "#/components/examples/ValidationError"),
                                            @ExampleObject(name = "Currency Mismatch", ref = "#/components/examples/CurrencyMismatch")
                                    }
                            )),
                    @ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content(schema = @Schema(implementation = ErrorExamples.WalletNotFound.class))),
                    @ApiResponse(responseCode = "422", description = "Unprocessable Entity (insufficient funds)", content = @Content(schema = @Schema(implementation = ErrorExamples.TransactionFailed.class))),
                    @ApiResponse(responseCode = "423", description = "Wallet locked (SUSPENDED)", content = @Content(schema = @Schema(implementation = ErrorExamples.WalletLocked.class))),
                    @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(schema = @Schema(implementation = ErrorExamples.RateLimitExceeded.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorExamples.InternalServerError.class)))
            })
    @PostMapping(value = "/{id}/withdraw", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionResponse> withdraw(@PathVariable UUID id, @Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(walletApiService.withdraw(id, request));
    }

    @Operation(summary = "Transfer funds", description = "Transfers funds between two wallets. Atomic operation. This operation is idempotent.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transfer successful",
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(oneOf = {ErrorResponse.class, BadRequestErrorModel.class}),
                                    examples = {
                                            @ExampleObject(name = "Validation Error", ref = "#/components/examples/ValidationError"),
                                            @ExampleObject(name = "Currency Mismatch", ref = "#/components/examples/CurrencyMismatch")
                                    }
                            )),
                    @ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content(schema = @Schema(implementation = ErrorExamples.WalletNotFound.class))),
                    @ApiResponse(responseCode = "422", description = "Unprocessable Entity (insufficient funds)", content = @Content(schema = @Schema(implementation = ErrorExamples.TransactionFailed.class))),
                    @ApiResponse(responseCode = "423", description = "Wallet locked (SUSPENDED)", content = @Content(schema = @Schema(implementation = ErrorExamples.WalletLocked.class))),
                    @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(schema = @Schema(implementation = ErrorExamples.RateLimitExceeded.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorExamples.InternalServerError.class)))
            })
    @PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(walletApiService.transfer(request));
    }

    @Operation(summary = "Get wallet transactions", description = "Retrieves a list of transactions for a specific wallet.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the wallet",
                            example = "cfb87bfc-6e9d-407e-8cc9-9f11e48bd390", in = ParameterIn.PATH, required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content(schema = @Schema(implementation = ErrorExamples.WalletNotFound.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorExamples.InternalServerError.class)))
            })
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable UUID id) {
        return ResponseEntity.ok(walletApiService.getTransactions(id));
    }
}

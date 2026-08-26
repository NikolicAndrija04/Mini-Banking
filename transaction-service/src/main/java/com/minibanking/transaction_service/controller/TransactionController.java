package com.minibanking.transaction_service.controller;

import com.minibanking.transaction_service.dto.TransactionResponse;
import com.minibanking.transaction_service.dto.TransferRequest;
import com.minibanking.transaction_service.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.minibanking.transaction_service.dto.TransactionDescriptionRequest;

@Tag(
        name = "Transactions",
        description = "Operations for managing bank transactions"
)
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(
            summary = "Transfer money",
            description = "Transfers money between two accounts using Account Service through Feign"
    )
    @ApiResponse(responseCode = "201", description = "Transfer completed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid transfer")
    @ApiResponse(responseCode = "503", description = "Account service unavailable")
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.transfer(request));
    }

    @Operation(
            summary = "Get transaction by ID",
            description = "Returns a transaction with the specified ID"
    )
    @ApiResponse(responseCode = "200", description = "Transaction found")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                transactionService.getById(id)
        );
    }

    @Operation(
            summary = "Update transaction description",
            description = "Updates the description of an existing transaction"
    )
    @ApiResponse(responseCode = "200", description = "Transaction updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @PutMapping("/{id}/description")
    public ResponseEntity<TransactionResponse> updateDescription(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDescriptionRequest request
    ) {
        return ResponseEntity.ok(
                transactionService.updateDescription(id, request)
        );
    }

    @Operation(
            summary = "Delete transaction",
            description = "Deletes a transaction with the specified ID"
    )
    @ApiResponse(responseCode = "204", description = "Transaction deleted successfully")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        transactionService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all transactions")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll() {
        return ResponseEntity.ok(transactionService.getAll());
    }

    @Operation(summary = "Get transactions by account ID")
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getByAccountId(
            @PathVariable Long accountId
    ) {
        return ResponseEntity.ok(
                transactionService.getByAccountId(accountId)
        );
    }
}
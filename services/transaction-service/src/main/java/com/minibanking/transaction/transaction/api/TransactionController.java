package com.minibanking.transaction.transaction.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.minibanking.transaction.transaction.domain.TransactionStatus;
import com.minibanking.transaction.transaction.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Auditable transfer orchestration")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfers")
    @Operation(
            summary = "Create and execute a transfer",
            description = "Calls account-service through a service-name Feign client. The idempotency key prevents duplicate requests."
    )
    @ApiResponse(responseCode = "201", description = "Transaction record created")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransferRequest request) {
        TransactionResponse created = transactionService.createAndExecute(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/transactions/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a transaction by id")
    public TransactionResponse findById(@PathVariable UUID id) {
        return transactionService.findById(id);
    }

    @GetMapping
    @Operation(summary = "List transactions with optional account and status filters")
    public List<TransactionResponse> findAll(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) TransactionStatus status
    ) {
        return transactionService.findAll(accountId, status);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction description")
    public TransactionResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request
    ) {
        return transactionService.update(id, request);
    }

    @PostMapping("/{id}/retry")
    @Operation(
            summary = "Retry a failed transaction",
            description = "Reuses the same transfer id, so account-service can safely return an idempotent replay."
    )
    public TransactionResponse retry(@PathVariable UUID id) {
        return transactionService.retry(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a failed transaction record")
    @ApiResponse(responseCode = "204", description = "Failed record deleted")
    @ApiResponse(responseCode = "409", description = "Completed records are immutable", content = @Content)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.minibanking.account.account.api;

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

import com.minibanking.account.account.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account lifecycle, balances and internal transfer operations")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "Create an account")
    @ApiResponse(responseCode = "201", description = "Account created")
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse created = accountService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an account by id")
    public AccountResponse findById(@PathVariable UUID id) {
        return accountService.findById(id);
    }

    @GetMapping
    @Operation(summary = "List accounts, optionally filtered by customer")
    public List<AccountResponse> findAll(@RequestParam(required = false) UUID customerId) {
        return accountService.findAll(customerId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace editable account data")
    public AccountResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateAccountRequest request) {
        return accountService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a zero-balance account")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deposits")
    @Operation(summary = "Deposit money to an active account")
    public AccountResponse deposit(@PathVariable UUID id, @Valid @RequestBody MoneyRequest request) {
        return accountService.deposit(id, request);
    }

    @PostMapping("/{id}/withdrawals")
    @Operation(summary = "Withdraw money from an active account")
    @ApiResponse(responseCode = "409", description = "Insufficient funds or inactive account", content = @Content)
    public AccountResponse withdraw(@PathVariable UUID id, @Valid @RequestBody MoneyRequest request) {
        return accountService.withdraw(id, request);
    }

    @PostMapping("/internal/transfers")
    @Operation(
            summary = "Move money atomically between accounts",
            description = "Internal endpoint used by transaction-service. transferId makes retries idempotent."
    )
    public InternalTransferResponse transfer(@Valid @RequestBody InternalTransferRequest request) {
        return accountService.transfer(request);
    }

    @GetMapping("/internal/transfers/{transferId}")
    @Operation(summary = "Get the result of an internal transfer")
    public InternalTransferResponse findTransfer(@PathVariable UUID transferId) {
        return accountService.findTransfer(transferId);
    }
}

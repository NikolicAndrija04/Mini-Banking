package com.minibanking.account_service.controller;

import com.minibanking.account_service.dto.AccountRequest;
import com.minibanking.account_service.dto.AccountResponse;
import com.minibanking.account_service.dto.AccountStatusRequest;
import com.minibanking.account_service.dto.BalanceRequest;
import com.minibanking.account_service.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.minibanking.account_service.dto.AccountDetailsResponse;

@Tag(
        name = "Accounts",
        description = "Operations for managing bank accounts"
)
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @Valid @RequestBody AccountRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountService.create(request));
    }

    @Operation(summary = "Get all accounts")
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAll() {
        return ResponseEntity.ok(accountService.getAll());
    }

    @Operation(summary = "Get account by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(accountService.getById(id));
    }

    @Operation(summary = "Get accounts by customer ID")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getByCustomerId(
            @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(
                accountService.getByCustomerId(customerId)
        );
    }

    @Operation(summary = "Delete account")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deposit money to account")
    @PostMapping("/{id}/deposit")
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable Long id,
            @Valid @RequestBody BalanceRequest request
    ) {
        return ResponseEntity.ok(
                accountService.deposit(id, request.getAmount())
        );
    }

    @Operation(summary = "Update account status")
    @PutMapping("/{id}/status")
    public ResponseEntity<AccountResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AccountStatusRequest request
    ) {
        return ResponseEntity.ok(
                accountService.updateStatus(id, request.getStatus())
        );
    }

    @Operation(summary = "Withdraw money from account")
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody BalanceRequest request
    ) {
        return ResponseEntity.ok(
                accountService.withdraw(id, request.getAmount())
        );
    }
    @Operation(
            summary = "Get account details",
            description = "Returns account data combined with customer data through Feign communication"
    )
    @ApiResponse(responseCode = "200", description = "Account details returned successfully")
    @ApiResponse(responseCode = "404", description = "Account or customer not found")
    @GetMapping("/{id}/details")
    public ResponseEntity<AccountDetailsResponse> getDetails(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                accountService.getDetails(id)
        );
    }
}
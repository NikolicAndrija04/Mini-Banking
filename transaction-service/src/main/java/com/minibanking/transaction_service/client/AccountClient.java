package com.minibanking.transaction_service.client;

import com.minibanking.transaction_service.dto.AccountResponse;
import com.minibanking.transaction_service.dto.BalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "account-service",
        fallback = AccountClientFallback.class
)
public interface AccountClient {

    @GetMapping("/api/accounts/{id}")
    AccountResponse getAccountById(@PathVariable("id") Long id);

    @PostMapping("/api/accounts/{id}/withdraw")
    AccountResponse withdraw(
            @PathVariable("id") Long id,
            @RequestBody BalanceRequest request
    );

    @PostMapping("/api/accounts/{id}/deposit")
    AccountResponse deposit(
            @PathVariable("id") Long id,
            @RequestBody BalanceRequest request
    );
}
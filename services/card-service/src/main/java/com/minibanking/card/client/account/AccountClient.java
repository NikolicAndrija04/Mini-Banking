package com.minibanking.card.client.account;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service", path = "/api/accounts")
public interface AccountClient {

    @GetMapping("/{id}")
    AccountSummary findById(@PathVariable UUID id);
}

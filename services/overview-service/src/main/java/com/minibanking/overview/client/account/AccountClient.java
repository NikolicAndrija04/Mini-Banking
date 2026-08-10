package com.minibanking.overview.client.account;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "account-service", path = "/api/accounts")
public interface AccountClient {

    @GetMapping("/{id}")
    AccountSummary findById(@PathVariable("id") UUID id);

    @GetMapping
    List<AccountSummary> findByCustomerId(@RequestParam("customerId") UUID customerId);
}

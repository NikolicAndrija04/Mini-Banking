package com.minibanking.overview.client.transaction;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "transaction-service", path = "/api/transactions")
public interface TransactionClient {

    @GetMapping
    List<TransactionSummary> findByAccountId(@RequestParam("accountId") UUID accountId);
}

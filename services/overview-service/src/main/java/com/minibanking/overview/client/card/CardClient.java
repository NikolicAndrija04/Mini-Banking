package com.minibanking.overview.client.card;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "card-service", path = "/api/cards")
public interface CardClient {

    @GetMapping
    List<CardSummary> findAll(
            @RequestParam(name = "customerId", required = false) UUID customerId,
            @RequestParam(name = "accountId", required = false) UUID accountId
    );
}

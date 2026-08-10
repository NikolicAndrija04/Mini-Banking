package com.minibanking.overview.client.customer;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", path = "/api/customers")
public interface CustomerClient {

    @GetMapping("/{id}")
    CustomerSummary findById(@PathVariable("id") UUID id);
}

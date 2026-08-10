package com.minibanking.transaction.client.account;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service", path = "/api/accounts")
public interface AccountClient {

    @PostMapping("/internal/transfers")
    AccountTransferResponse transfer(@RequestBody AccountTransferRequest request);
}

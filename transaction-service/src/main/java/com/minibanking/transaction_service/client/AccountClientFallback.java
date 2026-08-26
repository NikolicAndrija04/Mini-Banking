package com.minibanking.transaction_service.client;

import com.minibanking.transaction_service.dto.AccountResponse;
import com.minibanking.transaction_service.dto.BalanceRequest;
import com.minibanking.transaction_service.exception.AccountServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class AccountClientFallback implements AccountClient {

    @Override
    public AccountResponse getAccountById(Long id) {
        throw new AccountServiceUnavailableException();
    }

    @Override
    public AccountResponse withdraw(
            Long id,
            BalanceRequest request
    ) {
        throw new AccountServiceUnavailableException();
    }

    @Override
    public AccountResponse deposit(
            Long id,
            BalanceRequest request
    ) {
        throw new AccountServiceUnavailableException();
    }
}
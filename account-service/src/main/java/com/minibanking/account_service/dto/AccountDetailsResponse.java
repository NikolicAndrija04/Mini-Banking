package com.minibanking.account_service.dto;

public class AccountDetailsResponse {

    private AccountResponse account;
    private CustomerResponse customer;

    public AccountDetailsResponse() {
    }

    public AccountDetailsResponse(
            AccountResponse account,
            CustomerResponse customer
    ) {
        this.account = account;
        this.customer = customer;
    }

    public AccountResponse getAccount() {
        return account;
    }

    public CustomerResponse getCustomer() {
        return customer;
    }
}
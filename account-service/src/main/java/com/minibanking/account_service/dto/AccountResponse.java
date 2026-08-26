package com.minibanking.account_service.dto;

import com.minibanking.account_service.entity.AccountStatus;
import com.minibanking.account_service.entity.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {

    private Long id;
    private String accountNumber;
    private Long customerId;
    private BigDecimal balance;
    private Currency currency;
    private AccountStatus status;
    private LocalDateTime createdAt;

    public AccountResponse() {
    }

    public AccountResponse(
            Long id,
            String accountNumber,
            Long customerId,
            BigDecimal balance,
            Currency currency,
            AccountStatus status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
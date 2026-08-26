package com.minibanking.account_service.dto;

import com.minibanking.account_service.entity.Currency;
import jakarta.validation.constraints.NotNull;

public class AccountRequest {

    @NotNull(message = "Customer id is required")
    private Long customerId;

    @NotNull(message = "Currency is required")
    private Currency currency;

    public AccountRequest() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
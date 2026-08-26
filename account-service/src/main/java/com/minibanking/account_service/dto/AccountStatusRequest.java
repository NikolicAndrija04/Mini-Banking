package com.minibanking.account_service.dto;

import com.minibanking.account_service.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;

public class AccountStatusRequest {

    @NotNull(message = "Status is required")
    private AccountStatus status;

    public AccountStatusRequest() {
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
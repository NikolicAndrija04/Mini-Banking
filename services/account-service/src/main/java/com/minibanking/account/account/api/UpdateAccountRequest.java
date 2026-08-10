package com.minibanking.account.account.api;

import java.util.UUID;

import com.minibanking.account.account.domain.AccountStatus;
import com.minibanking.account.account.domain.AccountType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateAccountRequest(
        @NotNull UUID customerId,
        @NotBlank @Pattern(regexp = "^[A-Z0-9]{10,34}$") String accountNumber,
        @NotNull AccountType type,
        @NotNull AccountStatus status
) {
}

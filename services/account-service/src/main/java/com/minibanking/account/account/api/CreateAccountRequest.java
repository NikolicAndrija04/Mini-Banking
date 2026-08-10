package com.minibanking.account.account.api;

import java.math.BigDecimal;
import java.util.UUID;

import com.minibanking.account.account.domain.AccountType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateAccountRequest(
        @NotNull UUID customerId,
        @NotBlank @Pattern(regexp = "^[A-Z0-9]{10,34}$") String accountNumber,
        @NotNull AccountType type,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currencyCode,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal initialBalance
) {
}

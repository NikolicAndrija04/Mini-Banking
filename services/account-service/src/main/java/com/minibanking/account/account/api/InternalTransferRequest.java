package com.minibanking.account.account.api;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record InternalTransferRequest(
        @NotNull UUID transferId,
        @NotNull UUID sourceAccountId,
        @NotNull UUID destinationAccountId,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount
) {
}

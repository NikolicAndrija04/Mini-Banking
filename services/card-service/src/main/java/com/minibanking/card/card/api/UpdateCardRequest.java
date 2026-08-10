package com.minibanking.card.card.api;

import java.math.BigDecimal;

import com.minibanking.card.card.domain.CardStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCardRequest(
        @NotBlank @Size(max = 80) String cardholderName,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal dailyLimit,
        @NotNull CardStatus status
) {
}

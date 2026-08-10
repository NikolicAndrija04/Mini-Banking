package com.minibanking.account.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "minibanking.account")
public record AccountProperties(
        @NotNull @DecimalMin("0.01") BigDecimal maxTransferAmount
) {
}

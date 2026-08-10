package com.minibanking.card.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "minibanking.card")
public record CardProperties(
        @NotNull @DecimalMin("0.01") BigDecimal maxDailyLimit,
        @Min(1) @Max(10) int validityYears
) {
}

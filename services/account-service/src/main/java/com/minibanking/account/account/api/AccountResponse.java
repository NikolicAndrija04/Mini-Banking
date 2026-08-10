package com.minibanking.account.account.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.minibanking.account.account.domain.AccountStatus;
import com.minibanking.account.account.domain.AccountType;

public record AccountResponse(
        UUID id,
        UUID customerId,
        String accountNumber,
        AccountType type,
        String currencyCode,
        BigDecimal balance,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}

package com.minibanking.overview.client.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountSummary(
        UUID id,
        UUID customerId,
        String accountNumber,
        String type,
        String currencyCode,
        BigDecimal balance,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}

package com.minibanking.overview.client.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionSummary(
        UUID id,
        UUID idempotencyKey,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currencyCode,
        String description,
        String status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt
) {
}

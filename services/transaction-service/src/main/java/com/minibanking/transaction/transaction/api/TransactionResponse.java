package com.minibanking.transaction.transaction.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.minibanking.transaction.transaction.domain.TransactionStatus;

public record TransactionResponse(
        UUID id,
        UUID idempotencyKey,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currencyCode,
        String description,
        TransactionStatus status,
        String failureReason,
        BigDecimal sourceBalanceAfter,
        BigDecimal destinationBalanceAfter,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        boolean idempotentReplay
) {
}

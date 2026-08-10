package com.minibanking.transaction.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferCompletedDomainEvent(
        UUID transferId,
        UUID sourceCustomerId,
        UUID destinationCustomerId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currencyCode,
        Instant completedAt
) {
}

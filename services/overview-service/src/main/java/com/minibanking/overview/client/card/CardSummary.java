package com.minibanking.overview.client.card;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CardSummary(
        UUID id,
        UUID customerId,
        UUID accountId,
        String maskedPan,
        String lastFour,
        String type,
        String cardholderName,
        int expiryMonth,
        int expiryYear,
        BigDecimal dailyLimit,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}

package com.minibanking.card.card.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.minibanking.card.card.domain.CardStatus;
import com.minibanking.card.card.domain.CardType;

public record CardResponse(
        UUID id,
        UUID customerId,
        UUID accountId,
        String panToken,
        String maskedPan,
        String lastFour,
        CardType type,
        String cardholderName,
        int expiryMonth,
        int expiryYear,
        BigDecimal dailyLimit,
        CardStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}

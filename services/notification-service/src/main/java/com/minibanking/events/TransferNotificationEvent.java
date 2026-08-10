package com.minibanking.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferNotificationEvent(
        UUID eventId,
        UUID transferId,
        UUID customerId,
        String customerRole,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currencyCode,
        Instant occurredAt
) {
}

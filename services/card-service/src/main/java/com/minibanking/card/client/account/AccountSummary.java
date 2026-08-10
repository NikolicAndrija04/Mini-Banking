package com.minibanking.card.client.account;

import java.util.UUID;

public record AccountSummary(
        UUID id,
        UUID customerId,
        String accountNumber,
        String currencyCode,
        String status
) {
}

package com.minibanking.transaction.client.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountTransferResponse(
        UUID transferId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currencyCode,
        BigDecimal sourceBalanceAfter,
        BigDecimal destinationBalanceAfter,
        Instant completedAt,
        boolean idempotentReplay
) {
}

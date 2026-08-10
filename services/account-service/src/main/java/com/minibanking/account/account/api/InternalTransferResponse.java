package com.minibanking.account.account.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InternalTransferResponse(
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

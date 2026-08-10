package com.minibanking.transaction.client.account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountTransferRequest(
        UUID transferId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount
) {
}

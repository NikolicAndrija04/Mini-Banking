package com.minibanking.overview.aggregation;

import java.time.Instant;
import java.util.List;

import com.minibanking.overview.client.account.AccountSummary;
import com.minibanking.overview.client.card.CardSummary;
import com.minibanking.overview.client.transaction.TransactionSummary;

public record AccountOverviewResponse(
        String servedBy,
        Instant generatedAt,
        AccountSummary account,
        List<CardSummary> cards,
        List<TransactionSummary> transactions,
        List<DegradedService> degradedServices
) {
}

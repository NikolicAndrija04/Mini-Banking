package com.minibanking.overview.aggregation;

import java.time.Instant;
import java.util.List;

import com.minibanking.overview.client.account.AccountSummary;
import com.minibanking.overview.client.card.CardSummary;
import com.minibanking.overview.client.customer.CustomerSummary;
import com.minibanking.overview.client.transaction.TransactionSummary;

public record CustomerOverviewResponse(
        String servedBy,
        Instant generatedAt,
        CustomerSummary customer,
        List<AccountSummary> accounts,
        List<CardSummary> cards,
        List<TransactionSummary> transactions,
        List<DegradedService> degradedServices
) {
}

package com.minibanking.overview.aggregation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minibanking.overview.client.account.AccountSummary;
import com.minibanking.overview.client.card.CardSummary;
import com.minibanking.overview.client.customer.CustomerSummary;
import com.minibanking.overview.client.transaction.TransactionSummary;

@Service
public class OverviewService {

    private final ResilientBankingClients clients;
    private final String instanceId;

    public OverviewService(
            ResilientBankingClients clients,
            @Value("${INSTANCE_ID:${random.value}}") String instanceId
    ) {
        this.clients = clients;
        this.instanceId = instanceId;
    }

    public CustomerOverviewResponse customerOverview(UUID customerId) {
        ServiceCallResult<CustomerSummary> customer = clients.customer(customerId);
        ServiceCallResult<List<AccountSummary>> accounts = clients.accounts(customerId);
        ServiceCallResult<List<CardSummary>> cards = clients.cards(customerId, null);
        List<DegradedService> degraded = new ArrayList<>();
        addDegraded(degraded, customer);
        addDegraded(degraded, accounts);
        addDegraded(degraded, cards);

        Map<UUID, TransactionSummary> transactions = new LinkedHashMap<>();
        for (AccountSummary account : accounts.data()) {
            ServiceCallResult<List<TransactionSummary>> result = clients.transactions(account.id());
            addDegraded(degraded, result);
            result.data().forEach(transaction -> transactions.putIfAbsent(transaction.id(), transaction));
        }

        return new CustomerOverviewResponse(
                instanceId,
                Instant.now(),
                customer.data(),
                accounts.data(),
                cards.data(),
                List.copyOf(transactions.values()),
                distinct(degraded)
        );
    }

    public AccountOverviewResponse accountOverview(UUID accountId) {
        ServiceCallResult<AccountSummary> account = clients.account(accountId);
        ServiceCallResult<List<CardSummary>> cards = clients.cards(null, accountId);
        ServiceCallResult<List<TransactionSummary>> transactions = clients.transactions(accountId);
        List<DegradedService> degraded = new ArrayList<>();
        addDegraded(degraded, account);
        addDegraded(degraded, cards);
        addDegraded(degraded, transactions);
        return new AccountOverviewResponse(
                instanceId,
                Instant.now(),
                account.data(),
                cards.data(),
                transactions.data(),
                distinct(degraded)
        );
    }

    public InstanceResponse instance() {
        return new InstanceResponse("overview-service", instanceId, Instant.now());
    }

    private void addDegraded(List<DegradedService> degraded, ServiceCallResult<?> result) {
        if (!result.available()) {
            degraded.add(new DegradedService(result.service(), result.failureReason()));
        }
    }

    private List<DegradedService> distinct(List<DegradedService> degraded) {
        Map<String, DegradedService> byService = new LinkedHashMap<>();
        degraded.forEach(item -> byService.putIfAbsent(item.service(), item));
        return List.copyOf(byService.values());
    }
}

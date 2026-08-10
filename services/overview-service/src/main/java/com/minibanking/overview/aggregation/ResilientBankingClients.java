package com.minibanking.overview.aggregation;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.minibanking.overview.client.account.AccountClient;
import com.minibanking.overview.client.account.AccountSummary;
import com.minibanking.overview.client.card.CardClient;
import com.minibanking.overview.client.card.CardSummary;
import com.minibanking.overview.client.customer.CustomerClient;
import com.minibanking.overview.client.customer.CustomerSummary;
import com.minibanking.overview.client.transaction.TransactionClient;
import com.minibanking.overview.client.transaction.TransactionSummary;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class ResilientBankingClients {

    private final CustomerClient customerClient;
    private final AccountClient accountClient;
    private final CardClient cardClient;
    private final TransactionClient transactionClient;

    public ResilientBankingClients(
            CustomerClient customerClient,
            AccountClient accountClient,
            CardClient cardClient,
            TransactionClient transactionClient
    ) {
        this.customerClient = customerClient;
        this.accountClient = accountClient;
        this.cardClient = cardClient;
        this.transactionClient = transactionClient;
    }

    @Retry(name = "customerLookup")
    @CircuitBreaker(name = "customerLookup", fallbackMethod = "customerFallback")
    public ServiceCallResult<CustomerSummary> customer(UUID customerId) {
        return ServiceCallResult.available("customer-service", customerClient.findById(customerId));
    }

    public ServiceCallResult<CustomerSummary> customerFallback(UUID customerId, Throwable throwable) {
        return ServiceCallResult.unavailable("customer-service", null, throwable);
    }

    @Retry(name = "accountLookup")
    @CircuitBreaker(name = "accountLookup", fallbackMethod = "accountsFallback")
    public ServiceCallResult<List<AccountSummary>> accounts(UUID customerId) {
        return ServiceCallResult.available("account-service", accountClient.findByCustomerId(customerId));
    }

    public ServiceCallResult<List<AccountSummary>> accountsFallback(UUID customerId, Throwable throwable) {
        return ServiceCallResult.unavailable("account-service", List.of(), throwable);
    }

    @Retry(name = "accountLookup")
    @CircuitBreaker(name = "accountLookup", fallbackMethod = "accountFallback")
    public ServiceCallResult<AccountSummary> account(UUID accountId) {
        return ServiceCallResult.available("account-service", accountClient.findById(accountId));
    }

    public ServiceCallResult<AccountSummary> accountFallback(UUID accountId, Throwable throwable) {
        return ServiceCallResult.unavailable("account-service", null, throwable);
    }

    @Retry(name = "cardLookup")
    @CircuitBreaker(name = "cardLookup", fallbackMethod = "cardsFallback")
    public ServiceCallResult<List<CardSummary>> cards(UUID customerId, UUID accountId) {
        return ServiceCallResult.available("card-service", cardClient.findAll(customerId, accountId));
    }

    public ServiceCallResult<List<CardSummary>> cardsFallback(
            UUID customerId,
            UUID accountId,
            Throwable throwable
    ) {
        return ServiceCallResult.unavailable("card-service", List.of(), throwable);
    }

    @Retry(name = "transactionLookup")
    @CircuitBreaker(name = "transactionLookup", fallbackMethod = "transactionsFallback")
    public ServiceCallResult<List<TransactionSummary>> transactions(UUID accountId) {
        return ServiceCallResult.available("transaction-service", transactionClient.findByAccountId(accountId));
    }

    public ServiceCallResult<List<TransactionSummary>> transactionsFallback(UUID accountId, Throwable throwable) {
        return ServiceCallResult.unavailable("transaction-service", List.of(), throwable);
    }
}

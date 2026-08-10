package com.minibanking.overview.aggregation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.minibanking.overview.client.account.AccountClient;
import com.minibanking.overview.client.account.AccountSummary;
import com.minibanking.overview.client.card.CardClient;
import com.minibanking.overview.client.card.CardSummary;
import com.minibanking.overview.client.customer.CustomerClient;
import com.minibanking.overview.client.customer.CustomerSummary;
import com.minibanking.overview.client.transaction.TransactionClient;
import com.minibanking.overview.client.transaction.TransactionSummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "INSTANCE_ID=overview-test"
})
class OverviewServiceIntegrationTests {

    @Autowired
    private OverviewService overviewService;

    @MockitoBean
    private CustomerClient customerClient;

    @MockitoBean
    private AccountClient accountClient;

    @MockitoBean
    private CardClient cardClient;

    @MockitoBean
    private TransactionClient transactionClient;

    @Test
    void aggregatesFourServicesAndDeduplicatesTransactions() {
        UUID customerId = UUID.randomUUID();
        AccountSummary firstAccount = account(customerId);
        AccountSummary secondAccount = account(customerId);
        TransactionSummary sharedTransfer = transaction(firstAccount.id(), secondAccount.id());
        when(customerClient.findById(customerId)).thenReturn(customer(customerId));
        when(accountClient.findByCustomerId(customerId)).thenReturn(List.of(firstAccount, secondAccount));
        when(cardClient.findAll(customerId, null)).thenReturn(List.of(card(customerId, firstAccount.id())));
        when(transactionClient.findByAccountId(firstAccount.id())).thenReturn(List.of(sharedTransfer));
        when(transactionClient.findByAccountId(secondAccount.id())).thenReturn(List.of(sharedTransfer));

        CustomerOverviewResponse overview = overviewService.customerOverview(customerId);

        assertThat(overview.servedBy()).isEqualTo("overview-test");
        assertThat(overview.customer().id()).isEqualTo(customerId);
        assertThat(overview.accounts()).hasSize(2);
        assertThat(overview.cards()).hasSize(1);
        assertThat(overview.transactions()).containsExactly(sharedTransfer);
        assertThat(overview.degradedServices()).isEmpty();
    }

    @Test
    void returnsPartialResponseWhenCustomerServiceFails() {
        UUID customerId = UUID.randomUUID();
        when(customerClient.findById(customerId)).thenThrow(new IllegalStateException("offline"));
        when(accountClient.findByCustomerId(customerId)).thenReturn(List.of());
        when(cardClient.findAll(customerId, null)).thenReturn(List.of());

        CustomerOverviewResponse overview = overviewService.customerOverview(customerId);

        assertThat(overview.customer()).isNull();
        assertThat(overview.accounts()).isEmpty();
        assertThat(overview.degradedServices())
                .extracting(DegradedService::service)
                .containsExactly("customer-service");
    }

    private CustomerSummary customer(UUID customerId) {
        return new CustomerSummary(
                customerId,
                "Andrija",
                "Nikolic",
                "andrija@example.com",
                "+381641234567",
                "Novi Sad",
                "ACTIVE",
                Instant.now(),
                Instant.now()
        );
    }

    private AccountSummary account(UUID customerId) {
        return new AccountSummary(
                UUID.randomUUID(),
                customerId,
                "RS35105008123123123173",
                "CHECKING",
                "RSD",
                new BigDecimal("1000.00"),
                "ACTIVE",
                Instant.now(),
                Instant.now()
        );
    }

    private CardSummary card(UUID customerId, UUID accountId) {
        return new CardSummary(
                UUID.randomUUID(),
                customerId,
                accountId,
                "**** **** **** 1234",
                "1234",
                "DEBIT",
                "ANDRIJA NIKOLIC",
                8,
                2030,
                new BigDecimal("50000.00"),
                "ACTIVE",
                Instant.now(),
                Instant.now()
        );
    }

    private TransactionSummary transaction(UUID sourceId, UUID destinationId) {
        return new TransactionSummary(
                UUID.randomUUID(),
                UUID.randomUUID(),
                sourceId,
                destinationId,
                new BigDecimal("250.00"),
                "RSD",
                "Test transfer",
                "COMPLETED",
                null,
                Instant.now(),
                Instant.now(),
                Instant.now()
        );
    }
}

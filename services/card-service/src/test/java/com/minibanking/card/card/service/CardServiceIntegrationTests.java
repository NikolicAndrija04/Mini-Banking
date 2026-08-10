package com.minibanking.card.card.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.minibanking.card.card.api.CardResponse;
import com.minibanking.card.card.api.CreateCardRequest;
import com.minibanking.card.card.api.UpdateCardRequest;
import com.minibanking.card.card.domain.CardStatus;
import com.minibanking.card.card.domain.CardType;
import com.minibanking.card.client.account.AccountClient;
import com.minibanking.card.client.account.AccountSummary;
import com.minibanking.card.common.error.ConflictException;
import com.minibanking.card.common.error.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class CardServiceIntegrationTests {

    @Autowired
    private CardService cardService;

    @MockitoBean
    private AccountClient accountClient;

    @Test
    void validatesAccountAndCompletesCardCrudLifecycle() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        when(accountClient.findById(accountId)).thenReturn(account(accountId, customerId, "ACTIVE"));

        CardResponse created = cardService.create(request(customerId, accountId));

        assertThat(created.maskedPan()).startsWith("**** **** **** ");
        assertThat(created.maskedPan()).doesNotContain(created.panToken());
        assertThat(created.status()).isEqualTo(CardStatus.ACTIVE);
        verify(accountClient).findById(accountId);
        assertThat(cardService.findAll(customerId, null, null))
                .extracting(CardResponse::id)
                .contains(created.id());

        CardResponse updated = cardService.update(
                created.id(),
                new UpdateCardRequest("Andrija Nikolic", new BigDecimal("75000.00"), CardStatus.BLOCKED)
        );
        assertThat(updated.cardholderName()).isEqualTo("ANDRIJA NIKOLIC");
        cardService.delete(created.id());
        assertThatThrownBy(() -> cardService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsCardWhenAccountBelongsToAnotherCustomer() {
        UUID accountId = UUID.randomUUID();
        when(accountClient.findById(accountId)).thenReturn(account(accountId, UUID.randomUUID(), "ACTIVE"));

        assertThatThrownBy(() -> cardService.create(request(UUID.randomUUID(), accountId)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("does not belong");
    }

    private CreateCardRequest request(UUID customerId, UUID accountId) {
        return new CreateCardRequest(
                customerId,
                accountId,
                CardType.DEBIT,
                "Andrija Nikolic",
                new BigDecimal("50000.00")
        );
    }

    private AccountSummary account(UUID accountId, UUID customerId, String status) {
        return new AccountSummary(accountId, customerId, "RS35105008123123123173", "RSD", status);
    }
}

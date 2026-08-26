package com.minibanking.card_service.service;

import com.minibanking.card_service.client.AccountClient;
import com.minibanking.card_service.client.CustomerClient;
import com.minibanking.card_service.dto.AccountResponse;
import com.minibanking.card_service.dto.CardRequest;
import com.minibanking.card_service.dto.CardResponse;
import com.minibanking.card_service.dto.CustomerResponse;
import com.minibanking.card_service.entity.Card;
import com.minibanking.card_service.entity.CardStatus;
import com.minibanking.card_service.exception.AccountNotActiveException;
import com.minibanking.card_service.exception.AccountNotFoundException;
import com.minibanking.card_service.exception.CardNotFoundException;
import com.minibanking.card_service.repository.CardRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;

    public CardService(
            CardRepository cardRepository,
            AccountClient accountClient,
            CustomerClient customerClient
    ) {
        this.cardRepository = cardRepository;
        this.accountClient = accountClient;
        this.customerClient = customerClient;
    }

    public CardResponse create(CardRequest request) {

        AccountResponse account;

        try {
            account = accountClient.getAccountById(request.getAccountId());
        } catch (FeignException.NotFound exception) {
            throw new AccountNotFoundException(request.getAccountId());
        }

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new AccountNotActiveException(request.getAccountId());
        }

        Card card = new Card(
                generateCardNumber(),
                request.getAccountId(),
                request.getType(),
                CardStatus.ACTIVE,
                LocalDate.now().plusYears(5)
        );

        Card savedCard = cardRepository.save(card);

        return mapToResponse(savedCard);
    }

    public List<CardResponse> getAll() {
        return cardRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CardResponse getById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        return mapToResponse(card);
    }

    public List<CardResponse> getByAccountId(Long accountId) {
        return cardRepository.findByAccountId(accountId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CardResponse block(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        card.setStatus(CardStatus.BLOCKED);

        return mapToResponse(cardRepository.save(card));
    }

    public void delete(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardRepository.delete(card);
    }

    private String generateCardNumber() {
        String digits = UUID.randomUUID()
                .toString()
                .replaceAll("\\D", "");

        while (digits.length() < 16) {
            digits += (int) (Math.random() * 10);
        }

        return digits.substring(0, 16);
    }

    private CardResponse mapToResponse(Card card) {

        AccountResponse account =
                accountClient.getAccountById(card.getAccountId());

        CustomerResponse customer =
                customerClient.getCustomerById(account.getCustomerId());

        String cardHolderName =
                customer.getFirstName() + " " + customer.getLastName();

        return new CardResponse(
                card.getId(),
                card.getCardNumber(),
                card.getAccountId(),
                cardHolderName,
                card.getType(),
                card.getStatus(),
                card.getExpiryDate()
        );
    }
}
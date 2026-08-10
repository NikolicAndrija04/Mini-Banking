package com.minibanking.card.card.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minibanking.card.card.api.CardResponse;
import com.minibanking.card.card.api.CreateCardRequest;
import com.minibanking.card.card.api.UpdateCardRequest;
import com.minibanking.card.card.domain.CardStatus;
import com.minibanking.card.card.domain.PaymentCard;
import com.minibanking.card.card.repository.PaymentCardRepository;
import com.minibanking.card.client.account.AccountClient;
import com.minibanking.card.client.account.AccountSummary;
import com.minibanking.card.common.error.ConflictException;
import com.minibanking.card.common.error.ResourceNotFoundException;
import com.minibanking.card.config.CardProperties;

@Service
public class CardService {

    private final PaymentCardRepository cardRepository;
    private final AccountClient accountClient;
    private final CardProperties cardProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public CardService(
            PaymentCardRepository cardRepository,
            AccountClient accountClient,
            CardProperties cardProperties
    ) {
        this.cardRepository = cardRepository;
        this.accountClient = accountClient;
        this.cardProperties = cardProperties;
    }

    @Transactional
    public CardResponse create(CreateCardRequest request) {
        validateLimit(request.dailyLimit());
        validateActiveOwnedAccount(request.accountId(), request.customerId());
        String lastFour = "%04d".formatted(secureRandom.nextInt(10_000));
        PaymentCard card = new PaymentCard(
                request.customerId(),
                request.accountId(),
                UUID.randomUUID().toString(),
                lastFour,
                request.type(),
                normalizeName(request.cardholderName()),
                YearMonth.now().plusYears(cardProperties.validityYears()),
                request.dailyLimit()
        );
        return toResponse(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public CardResponse findById(UUID id) {
        return toResponse(requireCard(id));
    }

    @Transactional(readOnly = true)
    public List<CardResponse> findAll(UUID customerId, UUID accountId, CardStatus status) {
        return cardRepository.findAll().stream()
                .filter(card -> customerId == null || card.getCustomerId().equals(customerId))
                .filter(card -> accountId == null || card.getAccountId().equals(accountId))
                .filter(card -> status == null || card.getStatus() == status)
                .sorted(Comparator.comparing(PaymentCard::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CardResponse update(UUID id, UpdateCardRequest request) {
        PaymentCard card = requireCard(id);
        validateLimit(request.dailyLimit());
        if (request.status() == CardStatus.ACTIVE && card.getStatus() != CardStatus.ACTIVE) {
            validateActiveOwnedAccount(card.getAccountId(), card.getCustomerId());
        }
        card.update(normalizeName(request.cardholderName()), request.dailyLimit(), request.status());
        return toResponse(card);
    }

    @Transactional
    public CardResponse block(UUID id) {
        PaymentCard card = requireCard(id);
        card.block();
        return toResponse(card);
    }

    @Transactional
    public CardResponse activate(UUID id) {
        PaymentCard card = requireCard(id);
        validateActiveOwnedAccount(card.getAccountId(), card.getCustomerId());
        card.activate();
        return toResponse(card);
    }

    @Transactional
    public void delete(UUID id) {
        PaymentCard card = requireCard(id);
        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new ConflictException("An active card must be blocked before deletion");
        }
        cardRepository.delete(card);
    }

    private PaymentCard requireCard(UUID id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card " + id + " was not found"));
    }

    private void validateActiveOwnedAccount(UUID accountId, UUID customerId) {
        AccountSummary account = accountClient.findById(accountId);
        if (!account.customerId().equals(customerId)) {
            throw new ConflictException("The account does not belong to the requested customer");
        }
        if (!"ACTIVE".equals(account.status())) {
            throw new ConflictException("A card can only be linked to an active account");
        }
    }

    private void validateLimit(BigDecimal dailyLimit) {
        if (dailyLimit.compareTo(cardProperties.maxDailyLimit()) > 0) {
            throw new ConflictException("Daily limit exceeds the configured maximum of "
                    + cardProperties.maxDailyLimit());
        }
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ").toUpperCase();
    }

    private CardResponse toResponse(PaymentCard card) {
        return new CardResponse(
                card.getId(),
                card.getCustomerId(),
                card.getAccountId(),
                card.getPanToken(),
                card.getMaskedPan(),
                card.getLastFour(),
                card.getType(),
                card.getCardholderName(),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getDailyLimit(),
                card.getStatus(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}

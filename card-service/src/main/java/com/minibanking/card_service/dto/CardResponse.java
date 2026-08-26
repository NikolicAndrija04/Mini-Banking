package com.minibanking.card_service.dto;

import com.minibanking.card_service.entity.CardStatus;
import com.minibanking.card_service.entity.CardType;

import java.time.LocalDate;

public class CardResponse {

    private Long id;
    private String cardNumber;
    private Long accountId;
    private String cardHolderName;
    private CardType type;
    private CardStatus status;
    private LocalDate expiryDate;

    public CardResponse() {
    }

    public CardResponse(
            Long id,
            String cardNumber,
            Long accountId,
            String cardHolderName,
            CardType type,
            CardStatus status,
            LocalDate expiryDate
    ) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.accountId = accountId;
        this.cardHolderName = cardHolderName;
        this.type = type;
        this.status = status;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public CardType getType() {
        return type;
    }

    public CardStatus getStatus() {
        return status;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}
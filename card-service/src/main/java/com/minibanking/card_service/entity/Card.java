package com.minibanking.card_service.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false)
    private LocalDate expiryDate;

    public Card() {
    }

    public Card(
            String cardNumber,
            Long accountId,
            CardType type,
            CardStatus status,
            LocalDate expiryDate
    ) {
        this.cardNumber = cardNumber;
        this.accountId = accountId;
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

    public CardType getType() {
        return type;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}
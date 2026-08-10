package com.minibanking.card.card.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

import com.minibanking.card.common.error.ConflictException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "payment_cards",
        indexes = {
                @Index(name = "idx_card_customer", columnList = "customer_id"),
                @Index(name = "idx_card_account", columnList = "account_id")
        }
)
public class PaymentCard {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "pan_token", nullable = false, unique = true, updatable = false, length = 36)
    private String panToken;

    @Column(name = "masked_pan", nullable = false, updatable = false, length = 19)
    private String maskedPan;

    @Column(name = "last_four", nullable = false, updatable = false, length = 4)
    private String lastFour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private CardType type;

    @Column(name = "cardholder_name", nullable = false, length = 80)
    private String cardholderName;

    @Column(name = "expiry_month", nullable = false, updatable = false)
    private int expiryMonth;

    @Column(name = "expiry_year", nullable = false, updatable = false)
    private int expiryYear;

    @Column(name = "daily_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected PaymentCard() {
    }

    public PaymentCard(
            UUID customerId,
            UUID accountId,
            String panToken,
            String lastFour,
            CardType type,
            String cardholderName,
            YearMonth expiry,
            BigDecimal dailyLimit
    ) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.accountId = accountId;
        this.panToken = panToken;
        this.lastFour = lastFour;
        this.maskedPan = "**** **** **** " + lastFour;
        this.type = type;
        this.cardholderName = cardholderName;
        this.expiryMonth = expiry.getMonthValue();
        this.expiryYear = expiry.getYear();
        this.dailyLimit = dailyLimit;
        this.status = CardStatus.ACTIVE;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void update(String newCardholderName, BigDecimal newDailyLimit, CardStatus newStatus) {
        cardholderName = newCardholderName;
        dailyLimit = newDailyLimit;
        status = newStatus;
    }

    public void block() {
        if (status == CardStatus.EXPIRED) {
            throw new ConflictException("An expired card cannot be blocked");
        }
        status = CardStatus.BLOCKED;
    }

    public void activate() {
        if (status == CardStatus.EXPIRED) {
            throw new ConflictException("An expired card cannot be activated");
        }
        status = CardStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getPanToken() {
        return panToken;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public String getLastFour() {
        return lastFour;
    }

    public CardType getType() {
        return type;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public int getExpiryYear() {
        return expiryYear;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public CardStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

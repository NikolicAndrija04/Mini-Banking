package com.minibanking.transaction.transaction.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "bank_transactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_transaction_idempotency", columnNames = "idempotency_key"),
        indexes = {
                @Index(name = "idx_transaction_source", columnList = "source_account_id"),
                @Index(name = "idx_transaction_destination", columnList = "destination_account_id"),
                @Index(name = "idx_transaction_status", columnList = "status")
        }
)
public class BankTransaction {

    @Id
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private UUID idempotencyKey;

    @Column(name = "source_account_id", nullable = false, updatable = false)
    private UUID sourceAccountId;

    @Column(name = "destination_account_id", nullable = false, updatable = false)
    private UUID destinationAccountId;

    @Column(name = "source_customer_id")
    private UUID sourceCustomerId;

    @Column(name = "destination_customer_id")
    private UUID destinationCustomerId;

    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(length = 280)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "source_balance_after", precision = 19, scale = 2)
    private BigDecimal sourceBalanceAfter;

    @Column(name = "destination_balance_after", precision = 19, scale = 2)
    private BigDecimal destinationBalanceAfter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Version
    private long version;

    protected BankTransaction() {
    }

    public BankTransaction(
            UUID idempotencyKey,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String description
    ) {
        this.id = UUID.randomUUID();
        this.idempotencyKey = idempotencyKey;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.description = description;
        this.status = TransactionStatus.PENDING;
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

    public boolean matches(UUID sourceId, UUID destinationId, BigDecimal requestedAmount) {
        return sourceAccountId.equals(sourceId)
                && destinationAccountId.equals(destinationId)
                && amount.compareTo(requestedAmount) == 0;
    }

    public void complete(
            UUID sourceCustomer,
            UUID destinationCustomer,
            String currency,
            BigDecimal sourceBalance,
            BigDecimal destinationBalance,
            Instant accountCompletionTime
    ) {
        sourceCustomerId = sourceCustomer;
        destinationCustomerId = destinationCustomer;
        currencyCode = currency;
        sourceBalanceAfter = sourceBalance;
        destinationBalanceAfter = destinationBalance;
        completedAt = accountCompletionTime == null ? Instant.now() : accountCompletionTime;
        failureReason = null;
        status = TransactionStatus.COMPLETED;
    }

    public void fail(String reason) {
        failureReason = reason;
        status = TransactionStatus.FAILED;
    }

    public void prepareRetry() {
        failureReason = null;
        status = TransactionStatus.PENDING;
    }

    public void updateDescription(String newDescription) {
        description = newDescription;
    }

    public UUID getId() {
        return id;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public UUID getSourceCustomerId() {
        return sourceCustomerId;
    }

    public UUID getDestinationCustomerId() {
        return destinationCustomerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getDescription() {
        return description;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public BigDecimal getSourceBalanceAfter() {
        return sourceBalanceAfter;
    }

    public BigDecimal getDestinationBalanceAfter() {
        return destinationBalanceAfter;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}

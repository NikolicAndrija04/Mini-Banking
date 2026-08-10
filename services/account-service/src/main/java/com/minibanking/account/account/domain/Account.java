package com.minibanking.account.account.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.minibanking.account.common.error.ConflictException;

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
        name = "bank_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uk_account_number", columnNames = "account_number"),
        indexes = @Index(name = "idx_account_customer", columnList = "customer_id")
)
public class Account {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "account_number", nullable = false, length = 34)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Account() {
    }

    public Account(
            UUID customerId,
            String accountNumber,
            AccountType type,
            String currencyCode,
            BigDecimal initialBalance,
            AccountStatus status
    ) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.currencyCode = currencyCode;
        this.balance = initialBalance;
        this.status = status;
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

    public void update(UUID customerId, String accountNumber, AccountType type, AccountStatus status) {
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.status = status;
    }

    public void credit(BigDecimal amount) {
        requireActive();
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        requireActive();
        if (balance.compareTo(amount) < 0) {
            throw new ConflictException("Account " + id + " has insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    public void requireActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new ConflictException("Account " + id + " is not active");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountType getType() {
        return type;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

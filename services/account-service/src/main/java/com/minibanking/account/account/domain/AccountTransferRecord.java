package com.minibanking.account.account.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "account_transfer_records")
public class AccountTransferRecord {

    @Id
    @Column(name = "transfer_id")
    private UUID transferId;

    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @Column(name = "destination_account_id", nullable = false)
    private UUID destinationAccountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "source_balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal sourceBalanceAfter;

    @Column(name = "destination_balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal destinationBalanceAfter;

    @Column(name = "completed_at", nullable = false, updatable = false)
    private Instant completedAt;

    protected AccountTransferRecord() {
    }

    public AccountTransferRecord(
            UUID transferId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currencyCode,
            BigDecimal sourceBalanceAfter,
            BigDecimal destinationBalanceAfter
    ) {
        this.transferId = transferId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.sourceBalanceAfter = sourceBalanceAfter;
        this.destinationBalanceAfter = destinationBalanceAfter;
    }

    @PrePersist
    void onCreate() {
        completedAt = Instant.now();
    }

    public boolean matches(UUID sourceId, UUID destinationId, BigDecimal requestedAmount) {
        return sourceAccountId.equals(sourceId)
                && destinationAccountId.equals(destinationId)
                && amount.compareTo(requestedAmount) == 0;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getSourceBalanceAfter() {
        return sourceBalanceAfter;
    }

    public BigDecimal getDestinationBalanceAfter() {
        return destinationBalanceAfter;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}

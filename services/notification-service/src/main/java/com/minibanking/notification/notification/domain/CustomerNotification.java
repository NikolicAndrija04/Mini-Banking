package com.minibanking.notification.notification.domain;

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
        name = "customer_notifications",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_event", columnNames = "event_id"),
        indexes = @Index(name = "idx_notification_customer", columnList = "customer_id")
)
public class CustomerNotification {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40, updatable = false)
    private NotificationType type;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Version
    private long version;

    protected CustomerNotification() {
    }

    public CustomerNotification(
            UUID eventId,
            UUID customerId,
            NotificationType type,
            String title,
            String message
    ) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.customerId = customerId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.status = NotificationStatus.UNREAD;
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

    public void update(String newTitle, String newMessage, NotificationStatus newStatus) {
        title = newTitle;
        message = newMessage;
        applyStatus(newStatus);
    }

    public void markRead() {
        applyStatus(NotificationStatus.READ);
    }

    private void applyStatus(NotificationStatus newStatus) {
        status = newStatus;
        readAt = newStatus == NotificationStatus.READ ? Instant.now() : null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getReadAt() {
        return readAt;
    }
}

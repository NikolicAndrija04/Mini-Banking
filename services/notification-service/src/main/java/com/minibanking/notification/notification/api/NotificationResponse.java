package com.minibanking.notification.notification.api;

import java.time.Instant;
import java.util.UUID;

import com.minibanking.notification.notification.domain.NotificationStatus;
import com.minibanking.notification.notification.domain.NotificationType;

public record NotificationResponse(
        UUID id,
        UUID eventId,
        UUID customerId,
        NotificationType type,
        String title,
        String message,
        NotificationStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant readAt
) {
}

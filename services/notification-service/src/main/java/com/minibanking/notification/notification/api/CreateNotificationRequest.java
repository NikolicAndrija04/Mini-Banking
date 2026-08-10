package com.minibanking.notification.notification.api;

import java.util.UUID;

import com.minibanking.notification.notification.domain.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        UUID eventId,
        @NotNull UUID customerId,
        @NotNull NotificationType type,
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 1000) String message
) {
}

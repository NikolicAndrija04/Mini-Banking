package com.minibanking.notification.notification.api;

import com.minibanking.notification.notification.domain.NotificationStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateNotificationRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 1000) String message,
        @NotNull NotificationStatus status
) {
}

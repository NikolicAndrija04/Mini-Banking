package com.minibanking.notification.notification.service;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.minibanking.notification.common.error.ConflictException;
import com.minibanking.notification.common.error.ResourceNotFoundException;
import com.minibanking.notification.notification.api.CreateNotificationRequest;
import com.minibanking.notification.notification.api.NotificationResponse;
import com.minibanking.notification.notification.api.UpdateNotificationRequest;
import com.minibanking.notification.notification.domain.NotificationStatus;
import com.minibanking.notification.notification.domain.NotificationType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class NotificationServiceIntegrationTests {

    @Autowired
    private NotificationService notificationService;

    @Test
    void completesNotificationCrudLifecycle() {
        UUID customerId = UUID.randomUUID();
        NotificationResponse created = notificationService.create(request(UUID.randomUUID(), customerId));

        assertThat(created.status()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(notificationService.findAll(customerId, null, NotificationStatus.UNREAD))
                .extracting(NotificationResponse::id)
                .contains(created.id());
        NotificationResponse updated = notificationService.update(
                created.id(),
                new UpdateNotificationRequest("Transfer received", "Your balance changed.", NotificationStatus.READ)
        );
        assertThat(updated.readAt()).isNotNull();

        notificationService.delete(created.id());
        assertThatThrownBy(() -> notificationService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void preventsDuplicateEventNotifications() {
        UUID eventId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        notificationService.create(request(eventId, customerId));

        assertThatThrownBy(() -> notificationService.create(request(eventId, customerId)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    private CreateNotificationRequest request(UUID eventId, UUID customerId) {
        return new CreateNotificationRequest(
                eventId,
                customerId,
                NotificationType.TRANSFER_COMPLETED,
                "Transfer completed",
                "Your transfer was processed successfully."
        );
    }
}

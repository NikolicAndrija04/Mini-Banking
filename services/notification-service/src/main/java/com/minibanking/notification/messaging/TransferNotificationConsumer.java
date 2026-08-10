package com.minibanking.notification.messaging;

import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.minibanking.events.TransferNotificationEvent;
import com.minibanking.notification.notification.api.CreateNotificationRequest;
import com.minibanking.notification.notification.api.NotificationResponse;
import com.minibanking.notification.notification.domain.NotificationType;
import com.minibanking.notification.notification.service.NotificationService;

@Component
public class TransferNotificationConsumer {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public TransferNotificationConsumer(
            NotificationService notificationService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMessagingConfiguration.TRANSFER_NOTIFICATION_QUEUE)
    public void consume(TransferNotificationEvent event) {
        String destinationRole = "DESTINATION".equals(event.customerRole()) ? "received" : "sent";
        Optional<NotificationResponse> created = notificationService.createIfEventIsNew(new CreateNotificationRequest(
                event.eventId(),
                event.customerId(),
                NotificationType.TRANSFER_COMPLETED,
                "Transfer " + destinationRole,
                "Transfer " + event.transferId() + " for " + event.amount() + " "
                        + event.currencyCode() + " was completed."
        ));
        created.ifPresent(notification -> messagingTemplate.convertAndSend(
                "/topic/notifications/" + event.customerId(),
                notification
        ));
    }
}

package com.minibanking.transaction.messaging;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.minibanking.events.TransferNotificationEvent;

@Component
public class TransferEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public TransferEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(TransferCompletedDomainEvent event) {
        publish(event, event.sourceCustomerId(), "SOURCE");
        publish(event, event.destinationCustomerId(), "DESTINATION");
    }

    private void publish(TransferCompletedDomainEvent event, UUID customerId, String role) {
        TransferNotificationEvent notificationEvent = new TransferNotificationEvent(
                stableEventId(event.transferId(), role),
                event.transferId(),
                customerId,
                role,
                event.sourceAccountId(),
                event.destinationAccountId(),
                event.amount(),
                event.currencyCode(),
                event.completedAt()
        );
        rabbitTemplate.convertAndSend(
                RabbitMessagingConfiguration.EVENTS_EXCHANGE,
                RabbitMessagingConfiguration.TRANSFER_COMPLETED_ROUTING_KEY,
                notificationEvent
        );
    }

    private UUID stableEventId(UUID transferId, String role) {
        return UUID.nameUUIDFromBytes((transferId + ":" + role).getBytes(StandardCharsets.UTF_8));
    }
}

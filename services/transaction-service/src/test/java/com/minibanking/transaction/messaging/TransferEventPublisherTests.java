package com.minibanking.transaction.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.minibanking.events.TransferNotificationEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransferEventPublisherTests {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishesStableEventsForBothCustomers() {
        TransferEventPublisher publisher = new TransferEventPublisher(rabbitTemplate);
        UUID transferId = UUID.randomUUID();
        TransferCompletedDomainEvent event = new TransferCompletedDomainEvent(
                transferId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("250.00"),
                "RSD",
                Instant.now()
        );

        publisher.publishAfterCommit(event);
        publisher.publishAfterCommit(event);

        ArgumentCaptor<Object> payloads = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, times(4)).convertAndSend(
                org.mockito.ArgumentMatchers.eq(RabbitMessagingConfiguration.EVENTS_EXCHANGE),
                org.mockito.ArgumentMatchers.eq(RabbitMessagingConfiguration.TRANSFER_COMPLETED_ROUTING_KEY),
                payloads.capture()
        );
        TransferNotificationEvent firstSource = (TransferNotificationEvent) payloads.getAllValues().get(0);
        TransferNotificationEvent secondSource = (TransferNotificationEvent) payloads.getAllValues().get(2);
        assertThat(firstSource.eventId()).isEqualTo(secondSource.eventId());
        assertThat(payloads.getAllValues().stream()
                .map(TransferNotificationEvent.class::cast)
                .map(TransferNotificationEvent::customerRole))
                .containsExactly("SOURCE", "DESTINATION", "SOURCE", "DESTINATION");
    }
}

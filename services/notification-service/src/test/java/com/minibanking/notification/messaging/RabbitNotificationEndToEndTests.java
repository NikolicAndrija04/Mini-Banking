package com.minibanking.notification.messaging;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import com.minibanking.events.TransferNotificationEvent;
import com.minibanking.notification.notification.api.NotificationResponse;
import com.minibanking.notification.notification.service.NotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DirtiesContext
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class RabbitNotificationEndToEndTests {

    @Container
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:4-management-alpine")
    );

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotificationService notificationService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void consumesRealRabbitMessageAndDeduplicatesRedelivery() throws InterruptedException {
        UUID eventId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        TransferNotificationEvent event = new TransferNotificationEvent(
                eventId,
                UUID.randomUUID(),
                customerId,
                "DESTINATION",
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("1250.00"),
                "RSD",
                Instant.now()
        );

        rabbitTemplate.convertAndSend(
                RabbitMessagingConfiguration.EVENTS_EXCHANGE,
                RabbitMessagingConfiguration.TRANSFER_COMPLETED_ROUTING_KEY,
                event
        );
        rabbitTemplate.convertAndSend(
                RabbitMessagingConfiguration.EVENTS_EXCHANGE,
                RabbitMessagingConfiguration.TRANSFER_COMPLETED_ROUTING_KEY,
                event
        );

        waitUntilNotificationExists(customerId, Duration.ofSeconds(15));

        assertThat(notificationService.findAll(customerId, null, null))
                .singleElement()
                .satisfies(notification -> assertThat(notification.eventId()).isEqualTo(eventId));
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/notifications/" + customerId),
                any(NotificationResponse.class)
        );
    }

    private void waitUntilNotificationExists(UUID customerId, Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            if (!notificationService.findAll(customerId, null, null).isEmpty()) {
                return;
            }
            Thread.sleep(200);
        }
        throw new AssertionError("Notification was not consumed from RabbitMQ within " + timeout);
    }
}

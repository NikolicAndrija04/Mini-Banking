package com.minibanking.notification.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMessagingConfiguration {

    public static final String EVENTS_EXCHANGE = "minibanking.events";
    public static final String TRANSFER_COMPLETED_ROUTING_KEY = "transfer.completed";
    public static final String TRANSFER_NOTIFICATION_QUEUE = "notification.transfer-events";
    public static final String DEAD_LETTER_EXCHANGE = "minibanking.dlx";
    public static final String DEAD_LETTER_QUEUE = "notification.transfer-events.dlq";
    public static final String DEAD_LETTER_ROUTING_KEY = "notification.transfer-events.failed";

    @Bean
    TopicExchange miniBankingEventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    DirectExchange miniBankingDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    Queue transferNotificationQueue() {
        return QueueBuilder.durable(TRANSFER_NOTIFICATION_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    Queue transferNotificationDeadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding transferNotificationBinding(Queue transferNotificationQueue, TopicExchange miniBankingEventsExchange) {
        return BindingBuilder.bind(transferNotificationQueue)
                .to(miniBankingEventsExchange)
                .with(TRANSFER_COMPLETED_ROUTING_KEY);
    }

    @Bean
    Binding transferNotificationDeadLetterBinding(
            Queue transferNotificationDeadLetterQueue,
            DirectExchange miniBankingDeadLetterExchange
    ) {
        return BindingBuilder.bind(transferNotificationDeadLetterQueue)
                .to(miniBankingDeadLetterExchange)
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    MessageConverter rabbitJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

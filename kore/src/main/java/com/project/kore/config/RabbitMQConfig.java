package com.project.kore.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione RabbitMQ per la chat asincrona: coda principale, Dead Letter Queue,
 * exchange diretto e relativo binding.
 */
@Configuration
public class RabbitMQConfig {

    public static final String CHAT_QUEUE        = "chat.messages.queue";
    public static final String CHAT_DLQ          = "chat.messages.dlq";
    public static final String CHAT_EXCHANGE     = "chat.exchange";
    public static final String CHAT_ROUTING_KEY  = "chat.message";

    // Coda principale durabile: i messaggi rifiutati in modo permanente finiscono in DLQ.
    @Bean
    public Queue chatQueue() {
        return QueueBuilder.durable(CHAT_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CHAT_DLQ)
                .build();
    }

    // Dead Letter Queue persistente: raccoglie i messaggi non elaborabili.
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(CHAT_DLQ, true);
    }

    // Exchange diretto per instradare i messaggi della chat.
    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(CHAT_EXCHANGE);
    }

    // Lega la coda principale all'exchange con la routing key chat.message.
    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue()).to(chatExchange()).with(CHAT_ROUTING_KEY);
    }

    // Serializza e deserializza i payload in JSON.
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // Factory dei listener: usa il converter Jackson e disabilita il re-enqueue
    // automatico dei messaggi rifiutati (li lasciamo gestire dalla DLQ).
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    // Template per l'invio dei messaggi, con converter Jackson.
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

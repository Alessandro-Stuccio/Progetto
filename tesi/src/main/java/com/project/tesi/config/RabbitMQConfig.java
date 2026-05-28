package com.project.tesi.config;

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
 * Configurazione RabbitMQ per la messaggistica asincrona della chat.
 * Definisce la coda principale ({@code chat.messages.queue}), la Dead Letter Queue
 * ({@code chat.messages.dlq}), l'exchange diretto e il binding.
 */
@Configuration
public class RabbitMQConfig {

    public static final String CHAT_QUEUE        = "chat.messages.queue";
    public static final String CHAT_DLQ          = "chat.messages.dlq";
    public static final String CHAT_EXCHANGE     = "chat.exchange";
    public static final String CHAT_ROUTING_KEY  = "chat.message";

    /**
     * Coda principale durabile con fallback sulla DLQ in caso di rifiuto
     * permanente del messaggio.
     *
     * @return la coda {@code chat.messages.queue}
     */
    @Bean
    public Queue chatQueue() {
        return QueueBuilder.durable(CHAT_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CHAT_DLQ)
                .build();
    }

    /**
     * Dead Letter Queue persistente che raccoglie i messaggi non elaborabili.
     *
     * @return la coda {@code chat.messages.dlq}
     */
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(CHAT_DLQ, true);
    }

    /**
     * Exchange diretto usato per l'instradamento dei messaggi chat.
     *
     * @return il {@link DirectExchange} {@code chat.exchange}
     */
    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(CHAT_EXCHANGE);
    }

    /**
     * Lega la coda principale all'exchange con routing key {@code chat.message}.
     *
     * @return il {@link Binding} configurato
     */
    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue()).to(chatExchange()).with(CHAT_ROUTING_KEY);
    }

    /**
     * Converter Jackson per serializzare/deserializzare i payload in JSON.
     *
     * @return il {@link MessageConverter} basato su Jackson
     */
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * Factory per i listener RabbitMQ; usa il converter Jackson e disabilita
     * il re-enqueue automatico dei messaggi rifiutati.
     *
     * @param connectionFactory connessione al broker
     * @return la factory configurata
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    /**
     * Template RabbitMQ con converter Jackson per l'invio dei messaggi.
     *
     * @param connectionFactory connessione al broker
     * @return il {@link RabbitTemplate} configurato
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

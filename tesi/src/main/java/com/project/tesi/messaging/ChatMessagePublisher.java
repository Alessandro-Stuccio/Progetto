package com.project.tesi.messaging;

import com.project.tesi.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher RabbitMQ per i messaggi della chat. Pubblica {@link ChatMessagePayload}
 * sull'exchange {@code chat.exchange} con routing key {@code chat.message}
 * per il salvataggio asincrono.
 */
@Component
public class ChatMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public ChatMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Invia il payload del messaggio chat al broker RabbitMQ.
     *
     * @param chatId   identificativo della conversazione
     * @param senderId identificativo del mittente
     * @param content  testo del messaggio
     */
    public void publish(Long chatId, Long senderId, String content) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHAT_EXCHANGE,
                RabbitMQConfig.CHAT_ROUTING_KEY,
                new ChatMessagePayload(chatId, senderId, content));
    }
}

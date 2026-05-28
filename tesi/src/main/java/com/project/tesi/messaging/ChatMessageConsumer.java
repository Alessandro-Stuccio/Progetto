package com.project.tesi.messaging;

import com.project.tesi.config.RabbitMQConfig;
import com.project.tesi.service.ChatAsyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageConsumer.class);
    private final ChatAsyncService chatAsyncService;

    public ChatMessageConsumer(ChatAsyncService chatAsyncService) {
        this.chatAsyncService = chatAsyncService;
    }

    @RabbitListener(queues = RabbitMQConfig.CHAT_DLQ)
    public void handleDeadLetter(ChatMessagePayload payload) {
        log.error("[DLQ] Messaggio chat non consegnato: chatId={}, senderId={}, content={}",
                payload.chatId(), payload.senderId(), payload.content());
    }

    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void consume(ChatMessagePayload payload) {
        log.info("[RabbitMQ] Consume chat message chatId={} senderId={}", payload.chatId(), payload.senderId());
        try {
            chatAsyncService.saveChatMessage(payload.chatId(), payload.senderId(), payload.content());
        } catch (DataIntegrityViolationException e) {
            log.error("[RabbitMQ] Errore permanente (DataIntegrity) chatId={} senderId={}: {} — invio a DLQ senza retry",
                    payload.chatId(), payload.senderId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Errore permanente di integrità DB", e);
        } catch (Exception e) {
            log.error("[RabbitMQ] Errore durante save asincrono chatId={} senderId={}: {}",
                    payload.chatId(), payload.senderId(), e.getMessage(), e);
            throw e;
        }
    }
}

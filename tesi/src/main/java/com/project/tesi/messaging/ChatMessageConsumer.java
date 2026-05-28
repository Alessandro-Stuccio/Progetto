package com.project.tesi.messaging;

import com.project.tesi.config.RabbitMQConfig;
import com.project.tesi.service.ChatAsyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * Consumer RabbitMQ per i messaggi della chat. Ascolta {@code chat.messages.queue}
 * e salva i messaggi tramite {@link ChatAsyncService}. In caso di
 * {@link DataIntegrityViolationException} invia il messaggio alla DLQ senza retry.
 */
@Component
public class ChatMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageConsumer.class);
    private final ChatAsyncService chatAsyncService;

    public ChatMessageConsumer(ChatAsyncService chatAsyncService) {
        this.chatAsyncService = chatAsyncService;
    }

    /**
     * Logga i messaggi non consegnati ricevuti dalla Dead Letter Queue.
     *
     * @param payload il payload non elaborabile
     */
    @RabbitListener(queues = RabbitMQConfig.CHAT_DLQ)
    public void handleDeadLetter(ChatMessagePayload payload) {
        log.error("[DLQ] Messaggio chat non consegnato: chatId={}, senderId={}, content={}",
                payload.chatId(), payload.senderId(), payload.content());
    }

    /**
     * Consuma un payload dalla coda principale e chiama
     * {@link ChatAsyncService#saveChatMessage}. {@link DataIntegrityViolationException}
     * provoca il reindirizzamento alla DLQ senza retry; altre eccezioni innescano
     * il retry automatico di RabbitMQ.
     *
     * @param payload il messaggio da persistere
     */
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

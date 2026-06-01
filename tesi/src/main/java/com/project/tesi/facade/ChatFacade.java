package com.project.tesi.facade;

import com.project.tesi.dto.request.SendMessageRequest;
import com.project.tesi.dto.response.ChatMessageResponse;
import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ConversationPreviewResponse;
import com.project.tesi.model.User;

import java.util.List;

/**
 * Gestione delle chat, sia via WebSocket sia via REST.
 */
public interface ChatFacade {

    /**
     * Crea la chat tra i due utenti, oppure restituisce quella già esistente.
     */
    Long createChat(Long senderId, Long receiverId);

    /**
     * Invia un messaggio in una chat esistente.
     */
    ChatMessageResponse sendMessage(SendMessageRequest request, Long senderId);

    /**
     * Restituisce i messaggi della conversazione in modo paginato (pagina base 0).
     */
    List<ChatMessageResponse> getConversation(Long chatId, Long userId, int page, int size);

    /**
     * Restituisce l'anteprima di tutte le conversazioni dell'utente.
     */
    List<ConversationPreviewResponse> getUserConversations(Long userId);

    /**
     * Segna come letti tutti i messaggi non letti della chat.
     */
    void markAsRead(Long chatId, Long userId);

    /**
     * Conta tutti i messaggi non letti dell'utente, su tutte le sue chat.
     */
    Integer getTotalUnreadCount(Long userId);

    /**
     * Chiude una chat: operazione riservata al moderatore.
     */
    void closeChat(Long chatId, Long moderatorId);

    /**
     * Restituisce i dati di base del moderatore assegnato all'utente.
     */
    ClientBasicInfoResponse getModerator(User user);
}

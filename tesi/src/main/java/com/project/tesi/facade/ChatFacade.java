package com.project.tesi.facade;

import com.project.tesi.dto.request.SendMessageRequest;
import com.project.tesi.dto.response.ChatMessageResponse;
import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ConversationPreviewResponse;
import com.project.tesi.model.User;

import java.util.List;

/**
 * Facade per la gestione delle chat in tempo reale e REST.
 */
public interface ChatFacade {

    /**
     * Crea o recupera una chat tra due utenti.
     *
     * @param senderId   identificativo del mittente
     * @param receiverId identificativo del destinatario
     * @return identificativo della chat creata o esistente
     */
    Long createChat(Long senderId, Long receiverId);

    /**
     * Invia un messaggio in una chat esistente.
     *
     * @param request  dati del messaggio da inviare
     * @param senderId identificativo del mittente
     * @return dettagli del messaggio inviato
     */
    ChatMessageResponse sendMessage(SendMessageRequest request, Long senderId);

    /**
     * Restituisce i messaggi di una conversazione in modo paginato.
     *
     * @param chatId identificativo della chat
     * @param userId identificativo dell'utente richiedente
     * @param page   numero di pagina (base 0)
     * @param size   dimensione della pagina
     * @return lista paginata di messaggi della conversazione
     */
    List<ChatMessageResponse> getConversation(Long chatId, Long userId, int page, int size);

    /**
     * Restituisce l'anteprima di tutte le conversazioni dell'utente.
     *
     * @param userId identificativo dell'utente
     * @return lista delle anteprime delle conversazioni
     */
    List<ConversationPreviewResponse> getUserConversations(Long userId);

    /**
     * Segna come letti tutti i messaggi non letti in una chat.
     *
     * @param chatId identificativo della chat
     * @param userId identificativo dell'utente che ha letto i messaggi
     */
    void markAsRead(Long chatId, Long userId);

    /**
     * Restituisce il numero totale di messaggi non letti dall'utente.
     *
     * @param userId identificativo dell'utente
     * @return numero totale di messaggi non letti
     */
    Integer getTotalUnreadCount(Long userId);

    /**
     * Chiude una chat. Operazione riservata al moderatore.
     *
     * @param chatId      identificativo della chat da chiudere
     * @param moderatorId identificativo del moderatore che esegue l'operazione
     */
    void closeChat(Long chatId, Long moderatorId);

    /**
     * Restituisce le informazioni di base del moderatore assegnato all'utente.
     *
     * @param user utente per cui recuperare il moderatore
     * @return informazioni di base del moderatore
     */
    ClientBasicInfoResponse getModerator(User user);
}

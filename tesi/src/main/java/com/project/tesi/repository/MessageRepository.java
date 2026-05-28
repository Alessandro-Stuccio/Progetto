package com.project.tesi.repository;

import com.project.tesi.enums.MessageStatus;
import com.project.tesi.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository per l'accesso ai dati dell'entità {@link Message}.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Recupera i messaggi di una chat con paginazione, ordinati per timestamp
     * decrescente (i più recenti prima).
     *
     * @param chatId   ID della chat
     * @param pageable parametri di paginazione
     * @return lista paginata di messaggi
     */
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timeStamp DESC")
    List<Message> findMessagesByChatId(@Param("chatId") Long chatId, Pageable pageable);

    /**
     * Recupera l'ultimo messaggio inviato in una chat.
     *
     * @param chatId ID della chat
     * @return il messaggio più recente, o {@code null} se la chat è vuota
     */
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timeStamp DESC LIMIT 1")
    Message findLastMessageByChatId(@Param("chatId") Long chatId);

    /**
     * Conta i messaggi non letti da un utente in una chat specifica.
     * Sono considerati non letti i messaggi ricevuti (non inviati) dall'utente
     * con stato diverso da {@code readStatus}.
     *
     * @param chatId     ID della chat
     * @param userId     ID dell'utente destinatario
     * @param readStatus stato corrispondente a "letto"
     * @return numero di messaggi non letti
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.status != :readStatus " +
            "AND ((m.chat.user1.id = :userId AND m.sentByUser1 = false) " +
            "OR (m.chat.user2.id = :userId AND m.sentByUser1 = true))")
    int countUnreadMessagesByChatIdAndUserId(@Param("chatId") Long chatId,
                                             @Param("userId") Long userId,
                                             @Param("readStatus") MessageStatus readStatus);

    /**
     * Conta i messaggi non letti totali di un utente su tutte le sue chat.
     *
     * @param userId     ID dell'utente
     * @param readStatus stato corrispondente a "letto"
     * @return totale messaggi non letti dell'utente
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.status != :readStatus " +
            "AND m.chat.id IN (SELECT c.id FROM Chat c WHERE c.user1.id = :userId OR c.user2.id = :userId) " +
            "AND ((m.chat.user1.id = :userId AND m.sentByUser1 = false) " +
            "OR (m.chat.user2.id = :userId AND m.sentByUser1 = true))")
    int countTotalUnreadMessagesByUserId(@Param("userId") Long userId,
                                         @Param("readStatus") MessageStatus readStatus);

    /**
     * Aggiorna in bulk lo stato dei messaggi da {@code SENT} a {@code DELIVERED}
     * per i soli messaggi ricevuti (non inviati) dall'utente nella chat indicata.
     *
     * @param chatId    ID della chat
     * @param userId    ID dell'utente destinatario
     * @param sent      stato {@code SENT} da cui partire
     * @param delivered stato {@code DELIVERED} da impostare
     */
    @Modifying
    @Query("UPDATE Message m SET m.status = :delivered " +
           "WHERE m.chat.id = :chatId AND m.status = :sent " +
           "AND ((m.chat.user1.id = :userId AND m.sentByUser1 = false) " +
           "OR (m.chat.user2.id = :userId AND m.sentByUser1 = true))")
    void markMessagesAsDelivered(@Param("chatId") Long chatId,
                                  @Param("userId") Long userId,
                                  @Param("sent") MessageStatus sent,
                                  @Param("delivered") MessageStatus delivered);

    /**
     * Aggiorna in bulk tutti i messaggi non ancora in stato {@code READ}
     * a {@code READ} per i messaggi ricevuti dall'utente nella chat indicata.
     *
     * @param chatId ID della chat
     * @param userId ID dell'utente destinatario
     * @param read   stato {@code READ} da impostare
     */
    @Modifying
    @Query("UPDATE Message m SET m.status = :read " +
           "WHERE m.chat.id = :chatId AND m.status != :read " +
           "AND ((m.chat.user1.id = :userId AND m.sentByUser1 = false) " +
           "OR (m.chat.user2.id = :userId AND m.sentByUser1 = true))")
    void markMessagesAsRead(@Param("chatId") Long chatId,
                             @Param("userId") Long userId,
                             @Param("read") MessageStatus read);
}

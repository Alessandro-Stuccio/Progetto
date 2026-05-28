package com.project.tesi.repository;

import com.project.tesi.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per l'accesso ai dati dell'entità {@link Chat}.
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    /**
     * Cerca una chat esistente tra due utenti, indipendentemente dall'ordine in cui
     * sono stati memorizzati come {@code user1} e {@code user2}.
     *
     * @param userId1 ID del primo utente
     * @param userId2 ID del secondo utente
     * @return un Optional contenente la chat se esiste
     */
    @Query("SELECT c FROM Chat c WHERE (c.user1.id = :userId1 AND c.user2.id = :userId2) OR (c.user1.id = :userId2 AND c.user2.id = :userId1)")
    Optional<Chat> findChatBetweenUsers(@Param("userId1") long userId1, @Param("userId2") long userId2);

    /**
     * Recupera tutte le chat a cui appartiene un utente,
     * sia come {@code user1} sia come {@code user2}.
     *
     * @param userId ID dell'utente
     * @return lista delle chat dell'utente
     */
    @Query("SELECT c FROM Chat c WHERE c.user1.id = :userId OR c.user2.id = :userId")
    List<Chat> findAllChatsByUserId(@Param("userId") long userId);

    /**
     * Conta le chat in stato {@code OPEN} assegnate a un moderatore.
     * Usato per bilanciare il carico tra i moderatori disponibili.
     *
     * @param moderatorId ID del moderatore
     * @return numero di chat aperte del moderatore
     */
    @Query("""
            SELECT COUNT(c) FROM Chat c
            WHERE (c.user1.id = :moderatorId OR c.user2.id = :moderatorId)
            AND c.status = com.project.tesi.enums.ChatStatus.OPEN
            """)
    long countOpenChatsByModerator(@Param("moderatorId") long moderatorId);

}


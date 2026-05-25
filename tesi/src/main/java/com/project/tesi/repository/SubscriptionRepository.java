package com.project.tesi.repository;

import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

/**
 * Repository per l'accesso ai dati dell'entità {@link Subscription}.
 *
 * Fornisce query per recuperare gli abbonamenti attivi,
 * filtrati per utente o stato di attivazione.
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Cerca l'abbonamento attivo di un utente (passando l'entità User).
     * Ogni utente può avere al massimo un solo abbonamento con {@code active = true}.
     *
     * @param user l'utente titolare
     * @return un Optional contenente l'abbonamento attivo, vuoto se non presente
     */
    Optional<Subscription> findByUserAndActiveTrue(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Subscription s WHERE s.user = :user AND s.active = true")
    Optional<Subscription> findByUserAndActiveTrueWithLock(@Param("user") User user);

    /**
     * Cerca l'abbonamento attivo di un utente (passando direttamente l'ID).
     *
     * @param userId ID dell'utente titolare
     * @return un Optional contenente l'abbonamento attivo, vuoto se non presente
     */
    Optional<Subscription> findByUserIdAndActiveTrue(Long userId);

    /**
     * Restituisce tutti gli abbonamenti attualmente attivi nel sistema.
     * Usato dallo scheduler mensile per il rinnovo dei crediti.
     *
     * @return lista degli abbonamenti attivi
     */
    List<Subscription> findByActiveTrue();

    /**
     * Cerca l'abbonamento (attivo o non) di un utente tramite il suo ID.
     * Usato nella cancellazione dell'utente per eliminare anche la sottoscrizione.
     *
     * @param userId ID dell'utente
     * @return un Optional contenente l'abbonamento, vuoto se non presente
     */
    Optional<Subscription> findByUserId(Long userId);

    boolean existsByPlanId(Long planId);
}
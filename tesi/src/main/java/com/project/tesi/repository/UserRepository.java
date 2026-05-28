package com.project.tesi.repository;

import com.project.tesi.model.User;
import com.project.tesi.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per l'accesso ai dati dell'entità {@link User}.
 *
 * Estende {@link JpaRepository} che fornisce automaticamente le operazioni
 * CRUD di base (findAll, findById, save, delete, ecc.).
 * I metodi aggiuntivi usano le query derivate di Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Cerca un utente non cancellato per indirizzo email.
     * Usato dal meccanismo di autenticazione per caricare il {@code UserDetails}.
     *
     * @param email indirizzo email dell'utente
     * @return Optional con l'utente attivo, vuoto se non trovato
     */
    Optional<User> findByEmailAndDeletedFalse(String email);

    /**
     * Cerca un utente non cancellato per email escludendo un determinato ID.
     * Usato in fase di aggiornamento profilo per verificare l'unicità dell'email
     * senza escludere l'utente stesso.
     *
     * @param email indirizzo email da cercare
     * @param id    ID dell'utente da escludere dalla ricerca
     * @return Optional con l'eventuale conflitto, vuoto se email disponibile
     */
    Optional<User> findByEmailAndIdIsNotAndDeletedFalse(String email, Long id);

    /**
     * Restituisce tutti gli utenti non eliminati (soft-delete).
     *
     * @return lista degli utenti attivi
     */
    List<User> findAllByDeletedFalse();

    /**
     * Filtra gli utenti per ruolo, inclusi quelli eliminati.
     *
     * @param role ruolo da filtrare
     * @return lista degli utenti con il ruolo indicato
     */
    List<User> findByRole(Role role);

    /**
     * Filtra gli utenti attivi per ruolo.
     *
     * @param role ruolo da filtrare
     * @return lista degli utenti attivi con il ruolo indicato
     */
    List<User> findByRoleAndDeletedFalse(Role role);

    /**
     * Conta tutti i clienti assegnati a un personal trainer, inclusi i cancellati.
     *
     * @param pt il personal trainer
     * @return numero di clienti assegnati
     */
    long countByAssignedPT(User pt);

    /**
     * Conta i clienti attivi assegnati a un personal trainer.
     *
     * @param pt il personal trainer
     * @return numero di clienti attivi assegnati
     */
    long countByAssignedPTAndDeletedFalse(User pt);

    /**
     * Conta tutti i clienti assegnati a un nutrizionista, inclusi i cancellati.
     *
     * @param nutritionist il nutrizionista
     * @return numero di clienti assegnati
     */
    long countByAssignedNutritionist(User nutritionist);

    /**
     * Conta i clienti attivi assegnati a un nutrizionista.
     *
     * @param nutritionist il nutrizionista
     * @return numero di clienti attivi assegnati
     */
    long countByAssignedNutritionistAndDeletedFalse(User nutritionist);

    /**
     * Restituisce tutti i clienti assegnati a un personal trainer.
     *
     * @param pt il personal trainer
     * @return lista dei clienti
     */
    List<User> findByAssignedPT(User pt);

    /**
     * Restituisce i clienti attivi assegnati a un personal trainer.
     *
     * @param pt il personal trainer
     * @return lista dei clienti attivi
     */
    List<User> findByAssignedPTAndDeletedFalse(User pt);

    /**
     * Restituisce tutti i clienti assegnati a un nutrizionista.
     *
     * @param nutritionist il nutrizionista
     * @return lista dei clienti
     */
    List<User> findByAssignedNutritionist(User nutritionist);

    /**
     * Restituisce i clienti attivi assegnati a un nutrizionista.
     *
     * @param nutritionist il nutrizionista
     * @return lista dei clienti attivi
     */
    List<User> findByAssignedNutritionistAndDeletedFalse(User nutritionist);

    /**
     * Rimuove l'assegnazione PT da tutti gli utenti che avevano quel personal
     * trainer come assegnato. Usato prima del soft-delete del PT per evitare
     * riferimenti orfani.
     *
     * @param ptId ID del personal trainer da de-assegnare
     */
    @Modifying
    @Query("UPDATE User u SET u.assignedPT = null WHERE u.assignedPT.id = :ptId")
    void clearAssignedPT(@Param("ptId") Long ptId);

    /**
     * Rimuove l'assegnazione nutrizionista da tutti gli utenti che avevano quel
     * nutrizionista come assegnato. Analogo a {@link #clearAssignedPT} per il ruolo
     * {@code NUTRITIONIST}.
     *
     * @param nutriId ID del nutrizionista da de-assegnare
     */
    @Modifying
    @Query("UPDATE User u SET u.assignedNutritionist = null WHERE u.assignedNutritionist.id = :nutriId")
    void clearAssignedNutritionist(@Param("nutriId") Long nutriId);
}

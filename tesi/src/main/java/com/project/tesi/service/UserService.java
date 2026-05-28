package com.project.tesi.service;

import com.project.tesi.model.User;
import com.project.tesi.enums.Role;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Servizio per la gestione degli utenti della piattaforma.
 */
@Validated
public interface UserService {

    /**
     * Recupera un utente tramite il suo identificativo.
     *
     * @param id identificativo dell'utente
     * @return utente corrispondente
     */
    User getUserById(
            @NotNull(message = "l'id deve essere valorizzato")
            @Min(value = 1, message = "non esistono id negativi") Long id);

    /**
     * Recupera un utente tramite il suo indirizzo email (username).
     *
     * @param email indirizzo email dell'utente
     * @return utente corrispondente
     */
    User getUserByEmail(@NotNull String email);

    /**
     * Verifica se esiste un utente con l'indirizzo email specificato.
     *
     * @param email indirizzo email da verificare
     * @return {@code true} se l'email è già registrata, {@code false} altrimenti
     */
    boolean existsByEmail(@NotNull String email);

    /**
     * Persiste un utente nel database.
     *
     * @param user entità utente da salvare
     * @return utente salvato
     */
    User save(@NotNull User user);

    /**
     * Restituisce tutti gli utenti con un determinato ruolo.
     *
     * @param role ruolo da filtrare
     * @return lista degli utenti con quel ruolo
     */
    List<User> findByRole(@NotNull Role role);

    /**
     * Restituisce tutti gli utenti registrati nel sistema.
     *
     * @return lista di tutti gli utenti
     */
    List<User> findAll();

    /**
     * Conta i clienti assegnati a un personal trainer.
     *
     * @param pt utente personal trainer
     * @return numero di clienti assegnati
     */
    long countByAssignedPT(@NotNull User pt);

    /**
     * Conta i clienti assegnati a un nutrizionista.
     *
     * @param nutritionist utente nutrizionista
     * @return numero di clienti assegnati
     */
    long countByAssignedNutritionist(@NotNull User nutritionist);

    /**
     * Restituisce i clienti assegnati a un personal trainer.
     *
     * @param pt utente personal trainer
     * @return lista dei clienti assegnati
     */
    List<User> findByAssignedPT(@NotNull User pt);

    /**
     * Restituisce i clienti assegnati a un nutrizionista.
     *
     * @param nutritionist utente nutrizionista
     * @return lista dei clienti assegnati
     */
    List<User> findByAssignedNutritionist(@NotNull User nutritionist);

    /**
     * Verifica se esiste un utente con l'email data, escludendo un id specifico.
     * Utile per i controlli in fase di aggiornamento profilo.
     *
     * @param email     indirizzo email da verificare
     * @param excludeId identificativo dell'utente da escludere dalla ricerca
     * @return {@code true} se un altro utente usa quell'email, {@code false} altrimenti
     */
    boolean existsUserByEmailExcluding(String email, Long excludeId);

    /**
     * Elimina un utente tramite il suo identificativo.
     *
     * @param id identificativo dell'utente da eliminare
     */
    void deleteUser(Long id);

    /**
     * Codifica una password in chiaro utilizzando il meccanismo di hashing configurato.
     *
     * @param rawPassword password in chiaro da codificare
     * @return hash della password
     */
    String encodePassword(@NotNull String rawPassword);
}

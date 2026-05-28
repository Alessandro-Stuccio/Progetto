package com.project.tesi.service;

import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servizio per la gestione degli slot di disponibilità dei professionisti.
 * Utilizza lock per-slot (ReentrantLock) per prevenire il doppio booking.
 */
@Validated
public interface SlotService {

    /**
     * Crea e persiste una lista di slot di disponibilità.
     *
     * @param slots lista non vuota di slot da creare
     * @return lista degli slot salvati
     */
    List<Slot> createSlots(@NotNull @NotEmpty List<Slot> slots);

    /**
     * Restituisce gli slot disponibili (non ancora prenotati) di un professionista.
     *
     * @param professional utente professionista
     * @return lista degli slot disponibili
     */
    List<Slot> getAvailableSlots(@NotNull User professional);

    /**
     * Recupera uno slot tramite il suo identificativo.
     *
     * @param slotId identificativo dello slot
     * @return slot corrispondente
     */
    Slot getSlot(@NotNull @Min(1) Long slotId);

    /**
     * Registra la prenotazione di uno slot per un utente, acquisendo il lock per-slot.
     *
     * @param slotId      identificativo dello slot da prenotare
     * @param user        utente che effettua la prenotazione
     * @param meetingLink link per la videoconferenza associata
     * @return slot aggiornato con i dati della prenotazione
     */
    Slot saveBooking(@NotNull @Min(1) Long slotId, @NotNull User user, @NotBlank String meetingLink);

    /**
     * Elimina uno slot dal sistema.
     *
     * @param slotId identificativo dello slot da eliminare
     */
    void deleteSlot(@NotNull @Min(1) Long slotId);

    /**
     * Annulla la prenotazione di uno slot per un utente.
     *
     * @param slotId identificativo dello slot
     * @param userId identificativo dell'utente che annulla
     */
    void cancelBooking(@NotNull @Min(1) Long slotId, @NotNull @Min(1) Long userId);

    /**
     * Verifica se esiste già uno slot per un professionista in un determinato orario.
     *
     * @param professional utente professionista
     * @param startTime    data/ora di inizio dello slot
     * @return {@code true} se lo slot esiste, {@code false} altrimenti
     */
    boolean slotExists(@NotNull User professional, @NotNull LocalDateTime startTime);

    /**
     * Restituisce le prenotazioni recenti di un utente a partire da una data.
     *
     * @param user  utente client
     * @param since data/ora di inizio ricerca
     * @return lista degli slot prenotati dall'utente da {@code since}
     */
    List<Slot> findRecentByUser(@NotNull User user, @NotNull LocalDateTime since);

    /**
     * Restituisce le prenotazioni recenti ricevute da un professionista a partire da una data.
     *
     * @param professional utente professionista
     * @param since        data/ora di inizio ricerca
     * @return lista degli slot prenotati presso il professionista da {@code since}
     */
    List<Slot> findRecentByProfessional(@NotNull User professional, @NotNull LocalDateTime since);

    /**
     * Restituisce tutte le prenotazioni attive di un professionista.
     *
     * @param professional utente professionista
     * @return lista degli slot prenotati del professionista
     */
    List<Slot> findBookingsByProfessional(@NotNull User professional);

    /**
     * Restituisce le prenotazioni future di un utente a partire da una data/ora.
     *
     * @param user utente client
     * @param from data/ora di inizio ricerca
     * @return lista degli slot futuri prenotati dall'utente
     */
    List<Slot> findFutureByUser(@NotNull User user, @NotNull LocalDateTime from);

    /**
     * Restituisce tutti gli slot prenotati presenti nel sistema.
     *
     * @return lista di tutti gli slot con prenotazione attiva
     */
    List<Slot> getAllBookedSlots();

    /**
     * Registra sull'audit log la creazione di una nuova prenotazione.
     *
     * @param slot slot appena prenotato
     */
    void logBookingCreated(@NotNull Slot slot);

    /**
     * Restituisce gli slot prenotati di un professionista nella giornata specificata.
     *
     * @param professional utente professionista
     * @param dayStart     inizio della giornata
     * @param dayEnd       fine della giornata
     * @return lista degli slot del professionista nella giornata
     */
    List<Slot> findTodayByProfessional(@NotNull User professional,
                                       @NotNull LocalDateTime dayStart,
                                       @NotNull LocalDateTime dayEnd);

    /**
     * Verifica se esiste almeno una prenotazione tra un cliente e un professionista.
     *
     * @param clientId       identificativo del cliente
     * @param professionalId identificativo del professionista
     * @return {@code true} se esiste almeno una prenotazione, {@code false} altrimenti
     */
    boolean hasBookingBetween(@NotNull @Min(1) Long clientId, @NotNull @Min(1) Long professionalId);
}

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
 * Gestione degli slot di disponibilità dei professionisti e delle relative prenotazioni.
 * La prenotazione passa per un lock per-slot (ReentrantLock) per evitare l'overbooking.
 */
@Validated
public interface SlotService {

    List<Slot> createSlots(@NotNull @NotEmpty List<Slot> slots);

    /** Slot ancora liberi del professionista. */
    List<Slot> getAvailableSlots(@NotNull User professional);

    Slot getSlot(@NotNull @Min(1) Long slotId);

    /** Prenota lo slot per l'utente acquisendo il lock per-slot; ritorna lo slot aggiornato. */
    Slot saveBooking(@NotNull @Min(1) Long slotId, @NotNull User user, @NotBlank String meetingLink);

    void deleteSlot(@NotNull @Min(1) Long slotId);

    /** Annulla la prenotazione dell'utente su quello slot. */
    void cancelBooking(@NotNull @Min(1) Long slotId, @NotNull @Min(1) Long userId);

    /** Dice se il professionista ha già uno slot a quell'orario (per evitare duplicati). */
    boolean slotExists(@NotNull User professional, @NotNull LocalDateTime startTime);

    /** Slot prenotati dall'utente a partire dalla data indicata. */
    List<Slot> findRecentByUser(@NotNull User user, @NotNull LocalDateTime since);

    /** Slot prenotati presso il professionista a partire dalla data indicata. */
    List<Slot> findRecentByProfessional(@NotNull User professional, @NotNull LocalDateTime since);

    /** Tutte le prenotazioni del professionista. */
    List<Slot> findBookingsByProfessional(@NotNull User professional);

    /** Prenotazioni future dell'utente da una certa data/ora in poi. */
    List<Slot> findFutureByUser(@NotNull User user, @NotNull LocalDateTime from);

    List<Slot> getAllBookedSlots();

    /** Scrive sull'audit log la creazione di una prenotazione. */
    void logBookingCreated(@NotNull Slot slot);

    /** Slot prenotati del professionista nella giornata delimitata da dayStart/dayEnd. */
    List<Slot> findTodayByProfessional(@NotNull User professional,
                                       @NotNull LocalDateTime dayStart,
                                       @NotNull LocalDateTime dayEnd);

    /** Dice se cliente e professionista hanno almeno una prenotazione in comune (gate per le recensioni). */
    boolean hasBookingBetween(@NotNull @Min(1) Long clientId, @NotNull @Min(1) Long professionalId);
}

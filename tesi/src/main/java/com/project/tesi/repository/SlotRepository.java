package com.project.tesi.repository;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository per l'accesso ai dati dell'entità {@link Slot}.
 * Fornisce locking pessimistico su {@link #findByIdWithLock} per prevenire
 * il double-booking concorrente.
 */
public interface SlotRepository extends JpaRepository<Slot, Long> {

    /**
     * Verifica se esiste già uno slot per un professionista alla stessa ora,
     * usato per prevenire duplicati durante la generazione degli slot.
     *
     * @param professional il professionista proprietario dello slot
     * @param startTime    ora di inizio da verificare
     * @return {@code true} se lo slot esiste già
     */
    boolean existsByProfessionalAndStartTime(User professional, LocalDateTime startTime);

    /**
     * Recupera uno slot applicando un lock {@code PESSIMISTIC_WRITE} sulla riga.
     * Usato durante la fase di prenotazione per evitare race condition.
     *
     * @param id ID dello slot
     * @return Optional contenente lo slot bloccato
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findByIdWithLock(@Param("id") Long id);

    /**
     * Restituisce gli slot liberi di un professionista in un intervallo di tempo,
     * ordinati per ora di inizio crescente.
     *
     * @param profId ID del professionista
     * @param start  inizio dell'intervallo
     * @param end    fine dell'intervallo
     * @return lista degli slot disponibili
     */
    @Query("SELECT s FROM Slot s WHERE s.professional.id = :profId " +
            "AND s.bookedBy IS NULL " +
            "AND s.startTime BETWEEN :start AND :end " +
            "ORDER BY s.startTime ASC")
    List<Slot> findAvailableSlots(@Param("profId") Long profId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Restituisce tutti gli slot liberi (non prenotati) di un professionista,
     * senza filtro sulla data.
     *
     * @param professional il professionista
     * @return lista degli slot senza prenotazione
     */
    List<Slot> findByProfessionalAndBookedByIsNull(User professional);

    /**
     * Restituisce tutti gli slot (liberi e prenotati) di un professionista.
     *
     * @param professional il professionista
     * @return lista completa degli slot
     */
    List<Slot> findByProfessional(User professional);

    // ---- Query ex-BookingRepository ----

    /**
     * Restituisce tutti gli slot prenotati da un utente.
     *
     * @param bookedBy l'utente che ha effettuato le prenotazioni
     * @return lista degli slot prenotati
     */
    List<Slot> findByBookedBy(User bookedBy);

    /**
     * Restituisce le prenotazioni future di un utente, ordinate per data crescente.
     *
     * @param user l'utente
     * @param now  istante corrente come limite inferiore
     * @return lista degli slot futuri prenotati
     */
    @Query("SELECT s FROM Slot s WHERE s.bookedBy = :user AND s.startTime > :now ORDER BY s.startTime ASC")
    List<Slot> findFutureByBookedBy(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Restituisce gli slot prenotati per oggi da un professionista,
     * ordinati per ora di inizio crescente.
     *
     * @param professional il professionista
     * @param dayStart     inizio della giornata corrente
     * @param dayEnd       fine della giornata corrente
     * @return lista degli slot del giorno
     */
    @Query("SELECT s FROM Slot s WHERE s.professional = :professional " +
           "AND s.startTime >= :dayStart AND s.startTime < :dayEnd " +
           "AND s.bookedBy IS NOT NULL " +
           "ORDER BY s.startTime ASC")
    List<Slot> findTodayByProfessional(@Param("professional") User professional,
                                        @Param("dayStart") LocalDateTime dayStart,
                                        @Param("dayEnd") LocalDateTime dayEnd);

    /**
     * Restituisce le prenotazioni recenti di un utente a partire da una data,
     * ordinate per data di prenotazione decrescente. Usato per il feed attività.
     *
     * @param user  l'utente
     * @param since data/ora limite inferiore per {@code bookedAt}
     * @return lista delle prenotazioni recenti
     */
    @Query("SELECT s FROM Slot s WHERE s.bookedBy = :user AND s.bookedAt >= :since ORDER BY s.bookedAt DESC")
    List<Slot> findRecentByBookedBy(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * Restituisce le prenotazioni recenti gestite da un professionista a partire
     * da una data, ordinate per data di prenotazione decrescente.
     * Usato per il feed attività del professionista.
     *
     * @param professional il professionista
     * @param since        data/ora limite inferiore per {@code bookedAt}
     * @return lista delle prenotazioni recenti
     */
    @Query("SELECT s FROM Slot s WHERE s.professional = :professional AND s.bookedAt >= :since ORDER BY s.bookedAt DESC")
    List<Slot> findRecentByProfessional(@Param("professional") User professional, @Param("since") LocalDateTime since);

    /**
     * Verifica se un cliente ha mai effettuato almeno una prenotazione con un
     * professionista. Usato per abilitare la possibilità di lasciare una recensione.
     *
     * @param userId         ID del cliente
     * @param professionalId ID del professionista
     * @return {@code true} se esiste almeno una prenotazione
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Slot s " +
           "WHERE s.bookedBy.id = :userId AND s.professional.id = :professionalId")
    boolean existsByBookedByIdAndProfessionalId(@Param("userId") Long userId,
                                                 @Param("professionalId") Long professionalId);

    /**
     * Restituisce gli slot confermati imminenti per cui non è ancora stato inviato
     * il promemoria. Usato dallo scheduler {@code BookingReminderScheduler}.
     *
     * @param from limite inferiore dell'intervallo temporale
     * @param to   limite superiore dell'intervallo temporale
     * @return lista degli slot che necessitano promemoria
     */
    @Query("SELECT s FROM Slot s " +
           "WHERE s.status = com.project.tesi.enums.BookingStatus.CONFIRMED " +
           "AND s.reminderSent = false " +
           "AND s.bookedBy IS NOT NULL " +
           "AND s.startTime >= :from " +
           "AND s.startTime <= :to")
    List<Slot> findUpcomingNeedingReminder(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    /**
     * Restituisce tutti gli slot che risultano prenotati (con cliente e data di
     * prenotazione valorizzati). Usato per le statistiche admin.
     *
     * @return lista di tutti gli slot prenotati
     */
    @Query("SELECT s FROM Slot s WHERE s.bookedBy IS NOT NULL AND s.bookedAt IS NOT NULL")
    List<Slot> findAllBooked();

    /**
     * Verifica se uno slot specifico si trova in un determinato stato.
     *
     * @param slotId ID dello slot
     * @param status stato da verificare
     * @return {@code true} se lo slot esiste e ha lo stato indicato
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Slot s " +
           "WHERE s.id = :slotId AND s.status = :status")
    boolean existsByIdAndStatus(@Param("slotId") Long slotId, @Param("status") BookingStatus status);
}

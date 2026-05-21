package com.project.tesi.service;

import com.project.tesi.model.Booking;
import com.project.tesi.model.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.validation.annotation.Validated;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaccia del servizio per la gestione delle prenotazioni.
 * Verifica crediti, assegnazione e disponibilità dello slot prima di creare la prenotazione.
 */
@Validated
public interface BookingService {

    /** Crea una nuova prenotazione verificando tutti i vincoli di business. */
    BookingResponse createBooking(BookingRequest request, Long userId);

    /** Annulla una prenotazione esistente, liberando lo slot e riaccreditando il credito. */
    void cancelBooking(Long bookingId, Long userId);


    List<Booking> findRecentByUser(@NotNull User user, @NotNull @PastOrPresent LocalDateTime since);

    List<Booking> findRecentByProfessional(@NotNull User professional, @NotNull @PastOrPresent LocalDateTime since);

}

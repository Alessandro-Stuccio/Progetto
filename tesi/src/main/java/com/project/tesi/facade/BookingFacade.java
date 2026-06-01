package com.project.tesi.facade;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;

/**
 * Prenotazione e cancellazione di slot.
 */
public interface BookingFacade {

    /**
     * Prenota uno slot disponibile per conto dell'utente.
     */
    BookingResponse createBooking(BookingRequest request, Long userId);

    /**
     * Annulla una prenotazione esistente dell'utente.
     */
    void cancelBooking(Long bookingId, Long userId);
}

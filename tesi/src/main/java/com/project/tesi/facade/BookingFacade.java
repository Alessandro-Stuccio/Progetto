package com.project.tesi.facade;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;

/**
 * Facade per la prenotazione e cancellazione di slot.
 */
public interface BookingFacade {

    /**
     * Crea una nuova prenotazione per uno slot disponibile.
     *
     * @param request dati della prenotazione da effettuare
     * @param userId  identificativo dell'utente che effettua la prenotazione
     * @return dettagli della prenotazione creata
     */
    BookingResponse createBooking(BookingRequest request, Long userId);

    /**
     * Annulla una prenotazione esistente.
     *
     * @param bookingId identificativo della prenotazione da annullare
     * @param userId    identificativo dell'utente che richiede la cancellazione
     */
    void cancelBooking(Long bookingId, Long userId);
}

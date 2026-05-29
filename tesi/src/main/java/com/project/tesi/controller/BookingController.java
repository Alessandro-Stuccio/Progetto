package com.project.tesi.controller;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.facade.BookingFacade;
import com.project.tesi.model.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller REST per prenotazioni e cancellazioni.
 * Espone /api/bookings. Richiede autenticazione.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingFacade bookingFacade;

    public BookingController(BookingFacade bookingFacade) {
        this.bookingFacade = bookingFacade;
    }

    /**
     * Crea una prenotazione per uno slot disponibile.
     * Deduce i crediti dall'abbonamento attivo e usa locking pessimistico
     * per prevenire il double-booking concorrente.
     *
     * @param request dati della prenotazione (identificativo dello slot)
     * @param user    utente autenticato che effettua la prenotazione
     * @return {@link BookingResponse} con i dettagli della prenotazione confermata
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request,
                                                          @AuthenticationPrincipal User user) {
        log.info("Richiesta prenotazione slot {} da utente {}", request.slotId(), user.getId());
        BookingResponse response = bookingFacade.createBooking(request, user.getId());
        log.info("Prenotazione confermata: id={} utente={}", response.getId(), user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Annulla una prenotazione appartenente all'utente autenticato.
     * Il credito viene ripristinato sull'abbonamento solo se la cancellazione
     * avviene con più di 24 ore di anticipo rispetto all'orario dello slot.
     *
     * @param id   identificativo della prenotazione da annullare
     * @param user utente autenticato proprietario della prenotazione
     * @return messaggio di conferma con indicazione del credito riaccreditato
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelBooking(@PathVariable Long id,
                                                              @AuthenticationPrincipal User user) {
        log.info("Annullamento prenotazione id={} richiesto da utente {}", id, user.getId());
        bookingFacade.cancelBooking(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Prenotazione annullata con successo. Lo slot è stato liberato e il credito riaccreditato."));
    }

}

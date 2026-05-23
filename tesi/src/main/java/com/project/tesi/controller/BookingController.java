package com.project.tesi.controller;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.facade.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.tesi.model.User;
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
 * Endpoint REST per le prenotazioni.
 */
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Creazione e cancellazione prenotazioni")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final UserFacade userFacade;

    public BookingController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @Operation(summary = "Crea prenotazione", description = "Prenota uno slot. Deduce i crediti dall'abbonamento attivo. Usa locking per evitare double-booking.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prenotazione confermata"),
        @ApiResponse(responseCode = "400", description = "Slot non disponibile o crediti insufficienti"),
        @ApiResponse(responseCode = "401", description = "Non autenticato"),
        @ApiResponse(responseCode = "404", description = "Slot o utente non trovato")
    })
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request,
                                                          @AuthenticationPrincipal User user) {
        log.info("Richiesta prenotazione slot {} da utente {}", request.slotId(), user.getId());
        BookingResponse response = userFacade.createBooking(request, user.getId());
        log.info("Prenotazione confermata: id={} utente={}", response.getId(), user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Annulla prenotazione", description = "Annulla una prenotazione propria. Il credito viene restituito solo se mancano più di 24 ore.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prenotazione annullata"),
        @ApiResponse(responseCode = "400", description = "Annullamento non consentito (stato errato o meno di 24 ore)"),
        @ApiResponse(responseCode = "401", description = "Non autenticato"),
        @ApiResponse(responseCode = "403", description = "La prenotazione non appartiene all'utente")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelBooking(@PathVariable Long id,
                                                              @AuthenticationPrincipal User user) {
        log.info("Annullamento prenotazione id={} richiesto da utente {}", id, user.getId());
        userFacade.cancelBooking(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Prenotazione annullata con successo. Lo slot è stato liberato e il credito riaccreditato."));
    }

}

package com.project.tesi.controller;

import com.project.tesi.dto.response.ProfessionalSummaryDTO;
import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.ProfessionalFacade;
import com.project.tesi.facade.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.tesi.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST per la gestione degli slot e la ricerca dei professionisti.
 * Espone /api/professionals.
 */
@RestController
@RequestMapping("/api/professionals")
@Tag(name = "Professionals", description = "Gestione slot e recupero professionisti disponibili")
public class ProfessionalController {

    private final UserFacade userFacade;
    private final ProfessionalFacade professionalFacade;

    public ProfessionalController(UserFacade userFacade, ProfessionalFacade professionalFacade) {
        this.userFacade = userFacade;
        this.professionalFacade = professionalFacade;
    }

    /**
     * Restituisce la lista dei professionisti disponibili filtrata per ruolo.
     *
     * @param role ruolo richiesto (PERSONAL_TRAINER o NUTRITIONIST)
     * @return lista di {@link ProfessionalSummaryDTO}
     */
    @Operation(summary = "Lista professionisti", description = "Restituisce i professionisti disponibili filtrati per ruolo (PERSONAL_TRAINER o NUTRITIONIST).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista restituita"),
        @ApiResponse(responseCode = "400", description = "Ruolo non valido")
    })
    @GetMapping
    public ResponseEntity<List<ProfessionalSummaryDTO>> getProfessionals(@RequestParam Role role) {
        return ResponseEntity.ok(userFacade.findAvailableProfessionals(role));
    }

    /**
     * Restituisce gli slot disponibili di un professionista per il calendario di prenotazione.
     *
     * @param id ID del professionista
     * @return lista di {@link SlotDTO} con gli slot liberi
     */
    @Operation(summary = "Slot disponibili", description = "Restituisce gli slot liberi di un professionista per il calendario di prenotazione.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista slot"),
        @ApiResponse(responseCode = "404", description = "Professionista non trovato")
    })
    @GetMapping("/{id}/slots")
    public ResponseEntity<List<SlotDTO>> getProfessionalSlots(@PathVariable Long id) {
        return ResponseEntity.ok(professionalFacade.getAvailableSlots(id));
    }

    /**
     * Aggiunge nuovi slot disponibili al calendario del professionista autenticato.
     *
     * @param user  professionista autenticato
     * @param slots lista degli slot da creare
     * @return lista di {@link SlotDTO} degli slot creati
     */
    @Operation(summary = "Crea slot", description = "Aggiunge nuovi slot disponibili al calendario del professionista autenticato.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Slot creati"),
        @ApiResponse(responseCode = "401", description = "Non autenticato"),
        @ApiResponse(responseCode = "403", description = "Solo i professionisti possono creare slot")
    })
    @PostMapping("/slots")
    public ResponseEntity<List<SlotDTO>> createSlots(@AuthenticationPrincipal User user,
                                                      @RequestBody List<SlotDTO> slots) {
        return ResponseEntity.ok(professionalFacade.createSlots(user.getId(), slots));
    }

    /**
     * Rimuove uno slot dal calendario del professionista autenticato.
     *
     * @param slotId ID dello slot da eliminare
     * @param user   professionista autenticato proprietario dello slot
     * @return risposta 204 No Content in caso di successo
     */
    @Operation(summary = "Elimina slot", description = "Rimuove uno slot dal calendario del professionista autenticato.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Slot eliminato"),
        @ApiResponse(responseCode = "401", description = "Non autenticato"),
        @ApiResponse(responseCode = "403", description = "Lo slot non appartiene al professionista autenticato"),
        @ApiResponse(responseCode = "404", description = "Slot non trovato")
    })
    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId,
                                            @AuthenticationPrincipal User user) {
        professionalFacade.deleteSlot(slotId, user.getId());
        return ResponseEntity.noContent().build();
    }
}

package com.project.tesi.controller;

import com.project.tesi.dto.response.ProfessionalSummaryDTO;
import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.ProfessionalFacade;
import com.project.tesi.facade.UserFacade;
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
    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId,
                                            @AuthenticationPrincipal User user) {
        professionalFacade.deleteSlot(slotId, user.getId());
        return ResponseEntity.noContent().build();
    }
}

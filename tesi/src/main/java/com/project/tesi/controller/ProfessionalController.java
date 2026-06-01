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

/** Ricerca dei professionisti e gestione dei loro slot. /api/professionals. */
@RestController
@RequestMapping("/api/professionals")
public class ProfessionalController {

    private final UserFacade userFacade;
    private final ProfessionalFacade professionalFacade;

    public ProfessionalController(UserFacade userFacade, ProfessionalFacade professionalFacade) {
        this.userFacade = userFacade;
        this.professionalFacade = professionalFacade;
    }

    /** Professionisti disponibili filtrati per ruolo (PERSONAL_TRAINER o NUTRITIONIST). */
    @GetMapping
    public ResponseEntity<List<ProfessionalSummaryDTO>> getProfessionals(@RequestParam Role role) {
        return ResponseEntity.ok(userFacade.findAvailableProfessionals(role));
    }

    /** Slot liberi di un professionista, per il calendario di prenotazione. */
    @GetMapping("/{id}/slots")
    public ResponseEntity<List<SlotDTO>> getProfessionalSlots(@PathVariable Long id) {
        return ResponseEntity.ok(professionalFacade.getAvailableSlots(id));
    }

    /** Aggiunge slot al calendario del professionista autenticato. */
    @PostMapping("/slots")
    public ResponseEntity<List<SlotDTO>> createSlots(@AuthenticationPrincipal User user,
                                                      @RequestBody List<SlotDTO> slots) {
        return ResponseEntity.ok(professionalFacade.createSlots(user.getId(), slots));
    }

    /** Rimuove uno slot del professionista autenticato (deve esserne il proprietario). */
    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId,
                                            @AuthenticationPrincipal User user) {
        professionalFacade.deleteSlot(slotId, user.getId());
        return ResponseEntity.noContent().build();
    }
}

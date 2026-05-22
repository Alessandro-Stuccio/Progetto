package com.project.tesi.controller;

import com.project.tesi.dto.request.ProfileUpdateRequest;
import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ClientDashboardResponse;
import lombok.extern.slf4j.Slf4j;
import com.project.tesi.facade.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.tesi.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint REST per il profilo utente. Gestisce i dati anagrafici e il recupero della dashboard cliente.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Profilo utente e dashboard cliente")
public class UserController {

    private final UserFacade userFacade;

    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @Operation(summary = "Dashboard utente", description = "Restituisce profilo, abbonamento, professionisti assegnati e prossimi appuntamenti.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dashboard restituita"),
        @ApiResponse(responseCode = "401", description = "Token JWT mancante o non valido")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ClientDashboardResponse> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userFacade.getClientDashboard(user.getId()));
    }

    @Operation(summary = "Lista clienti", description = "Restituisce i clienti assegnati al professionista autenticato.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista restituita"),
        @ApiResponse(responseCode = "401", description = "Non autenticato"),
        @ApiResponse(responseCode = "403", description = "Solo i professionisti possono accedere a questa risorsa")
    })
    @GetMapping("/clients")
    public ResponseEntity<List<ClientBasicInfoResponse>> getClientsForProfessional(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userFacade.getClientsForProfessional(user.getId()));
    }

    @Operation(summary = "Aggiorna profilo", description = "Modifica nome, cognome, password o immagine profilo dell'utente autenticato.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profilo aggiornato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi"),
        @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal User user, @RequestBody ProfileUpdateRequest request) {
        userFacade.updateProfile(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Info admin", description = "Restituisce i dati di contatto dell'amministratore (usato per 'Contatta supporto').")
    @ApiResponse(responseCode = "200", description = "Dati admin restituiti")
    @GetMapping("/admin")
    public ResponseEntity<ClientBasicInfoResponse> getAdmin() {
        return ResponseEntity.ok(userFacade.getAdmin());
    }

    @Operation(summary = "Info moderatore", description = "Restituisce i dati del moderatore di supporto.")
    @ApiResponse(responseCode = "200", description = "Dati moderatore restituiti")
    @GetMapping("/moderator")
    public ResponseEntity<ClientBasicInfoResponse> getModerator() {
        return ResponseEntity.ok(userFacade.getModerator());
    }
}

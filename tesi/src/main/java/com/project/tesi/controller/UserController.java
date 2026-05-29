package com.project.tesi.controller;

import com.project.tesi.dto.request.ProfileUpdateRequest;
import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ClientDashboardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.project.tesi.facade.UserFacade;
import com.project.tesi.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint REST per il profilo utente. Gestisce i dati anagrafici e il recupero della dashboard cliente.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserFacade userFacade;

    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    /**
     * Restituisce la dashboard del cliente con profilo, abbonamento, professionisti assegnati
     * e prossimi appuntamenti.
     *
     * @param user cliente autenticato
     * @return il {@link ClientDashboardResponse} con i dati aggregati della dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ClientDashboardResponse> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userFacade.getClientDashboard(user.getId()));
    }

    /**
     * Restituisce la lista dei clienti assegnati al professionista autenticato.
     *
     * @param user professionista autenticato
     * @return lista di {@link ClientBasicInfoResponse}
     */
    @GetMapping("/clients")
    public ResponseEntity<List<ClientBasicInfoResponse>> getClientsForProfessional(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userFacade.getClientsForProfessional(user.getId()));
    }

    /**
     * Aggiorna i dati anagrafici o la password dell'utente autenticato.
     *
     * @param user    utente autenticato da aggiornare
     * @param request nuovi dati del profilo (nome, cognome, password, immagine)
     * @return risposta 200 OK senza corpo in caso di successo
     */
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal User user, @Valid @RequestBody ProfileUpdateRequest request) {
        userFacade.updateProfile(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * Restituisce i dati di contatto dell'amministratore.
     * Usato dal frontend per la sezione "Contatta supporto".
     *
     * @return {@link ClientBasicInfoResponse} con email e nome dell'amministratore
     */
    @GetMapping("/admin")
    public ResponseEntity<ClientBasicInfoResponse> getAdmin() {
        return ResponseEntity.ok(userFacade.getAdmin());
    }

}

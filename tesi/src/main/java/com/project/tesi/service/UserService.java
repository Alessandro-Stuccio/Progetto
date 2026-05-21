package com.project.tesi.service;

import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ClientDashboardResponse;
import com.project.tesi.dto.response.ProfessionalSummaryDTO;
import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.Role;

import java.util.List;

/**
 * Interfaccia del servizio per la gestione degli utenti.
 * Gestisce registrazione, profilo, dashboard cliente e lista clienti per professionisti.
 */
@Validated
public interface UserService {

    /** Registra un nuovo utente cliente e restituisce il profilo creato. */
    UserResponse registerUser(RegisterRequest request);

    /** Aggiorna il profilo dell'utente (nome, cognome, password, immagine). */
    void updateProfile(Long userId, com.project.tesi.dto.request.ProfileUpdateRequest request);

    /** Restituisce la lista dei professionisti disponibili per un dato ruolo. */
    List<ProfessionalSummaryDTO> findAvailableProfessionals(Role role);

    /** Restituisce la dashboard completa del cliente. */
    ClientDashboardResponse getClientDashboard(Long userId);

    /** Restituisce la lista dei clienti assegnati a un professionista. */
    List<ClientBasicInfoResponse> getClientsForProfessional(Long professionalId);

    /** Restituisce l'operatore di supporto (moderatore) da usare in chat assistenza. */
    ClientBasicInfoResponse getSupportOperator();

    /** Restituisce i dati dell'account Admin (per la chat di supporto). */
    ClientBasicInfoResponse getAdmin();

    User getUserById(@NotNull(message = "l'id deve essere valorizzato") @Min(value = 1, message = "non esistono id negativi") Long id);
}
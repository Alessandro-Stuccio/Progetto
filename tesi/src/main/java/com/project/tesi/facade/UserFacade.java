package com.project.tesi.facade;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.request.ProfileUpdateRequest;
import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ClientDashboardResponse;
import com.project.tesi.dto.response.ProfessionalSummaryDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.Role;

import java.util.List;

/**
 * Profilo utente, dashboard del cliente e attivazione abbonamenti.
 */
public interface UserFacade {

    /**
     * Dashboard del cliente: prenotazioni, crediti e abbonamento attivo.
     */
    ClientDashboardResponse getClientDashboard(Long userId);

    /**
     * Dati di base dell'amministratore di sistema.
     */
    ClientBasicInfoResponse getAdmin();

    /**
     * Aggiorna il profilo dell'utente.
     */
    void updateProfile(Long userId, ProfileUpdateRequest request);

    /**
     * I clienti associati al professionista.
     */
    List<ClientBasicInfoResponse> getClientsForProfessional(Long professionalId);

    /**
     * Attiva l'abbonamento scelto dall'utente (piano e frequenza dalla request).
     */
    SubscriptionResponse activateSubscription(PlanRequest request, Long userId);

    /**
     * Stato dell'abbonamento attivo dell'utente.
     */
    SubscriptionResponse getSubscriptionStatus(Long userId);

    /**
     * I professionisti disponibili per il ruolo indicato (es. PERSONAL_TRAINER, NUTRITIONIST).
     */
    List<ProfessionalSummaryDTO> findAvailableProfessionals(Role role);
}

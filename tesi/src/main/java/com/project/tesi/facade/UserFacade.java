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
 * Facade per il profilo utente, dashboard cliente e gestione abbonamenti.
 */
public interface UserFacade {

    /**
     * Restituisce la dashboard del cliente con prenotazioni, crediti e abbonamento attivo.
     *
     * @param userId identificativo del cliente
     * @return dati della dashboard del cliente
     */
    ClientDashboardResponse getClientDashboard(Long userId);

    /**
     * Restituisce le informazioni di base dell'amministratore di sistema.
     *
     * @return informazioni di base dell'admin
     */
    ClientBasicInfoResponse getAdmin();

    /**
     * Aggiorna il profilo dell'utente con i nuovi dati forniti.
     *
     * @param userId  identificativo dell'utente da aggiornare
     * @param request nuovi dati del profilo
     */
    void updateProfile(Long userId, ProfileUpdateRequest request);

    /**
     * Restituisce la lista dei clienti associati a un professionista.
     *
     * @param professionalId identificativo del professionista
     * @return lista dei clienti del professionista
     */
    List<ClientBasicInfoResponse> getClientsForProfessional(Long professionalId);

    /**
     * Attiva un abbonamento per l'utente in base al piano e alla frequenza di pagamento scelti.
     *
     * @param request dati del piano e della frequenza di pagamento
     * @param userId  identificativo dell'utente che attiva l'abbonamento
     * @return dettagli dell'abbonamento attivato
     */
    SubscriptionResponse activateSubscription(PlanRequest request, Long userId);

    /**
     * Restituisce lo stato corrente dell'abbonamento dell'utente.
     *
     * @param userId identificativo dell'utente
     * @return dettagli dell'abbonamento attivo
     */
    SubscriptionResponse getSubscriptionStatus(Long userId);

    /**
     * Restituisce i professionisti disponibili per il ruolo specificato.
     *
     * @param role ruolo del professionista da cercare (es. PERSONAL_TRAINER, NUTRITIONIST)
     * @return lista dei professionisti disponibili
     */
    List<ProfessionalSummaryDTO> findAvailableProfessionals(Role role);
}

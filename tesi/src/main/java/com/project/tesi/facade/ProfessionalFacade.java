package com.project.tesi.facade;

import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Facade per le operazioni del professionista: slot, prenotazioni e statistiche.
 */
public interface ProfessionalFacade {

    /**
     * Restituisce gli slot disponibili per un professionista.
     *
     * @param professionalId identificativo del professionista
     * @return lista degli slot disponibili
     */
    List<SlotDTO> getAvailableSlots(Long professionalId);

    /**
     * Crea nuovi slot per un professionista.
     *
     * @param professionalId identificativo del professionista
     * @param slots          lista di slot da creare
     * @return lista degli slot creati
     */
    List<SlotDTO> createSlots(Long professionalId, List<SlotDTO> slots);

    /**
     * Elimina uno slot esistente verificando che il richiedente ne sia il proprietario.
     *
     * @param slotId      identificativo dello slot da eliminare
     * @param requesterId identificativo del professionista che richiede l'eliminazione
     */
    void deleteSlot(Long slotId, Long requesterId);

    /**
     * Genera automaticamente gli slot a partire dal calendario settimanale del professionista
     * per un dato intervallo di date.
     *
     * @param professional entità del professionista per cui generare gli slot
     * @param startDate    data di inizio dell'intervallo
     * @param endDate      data di fine dell'intervallo
     */
    void generateSlotsFromSchedule(User professional, LocalDate startDate, LocalDate endDate);

    /**
     * Restituisce le prossime prenotazioni di un professionista.
     *
     * @param professionalId identificativo del professionista
     * @return lista delle prenotazioni future
     */
    List<BookingResponse> getUpcomingBookings(Long professionalId);

    /**
     * Restituisce le statistiche del professionista (prenotazioni, recensioni, crediti, ecc.).
     *
     * @param professionalId identificativo del professionista
     * @return riepilogo statistiche del professionista
     */
    ProfessionalStatsResponse getProfessionalStats(Long professionalId);
}

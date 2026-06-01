package com.project.tesi.facade;

import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Operazioni del professionista: slot, prenotazioni e statistiche.
 */
public interface ProfessionalFacade {

    /**
     * Gli slot ancora disponibili del professionista.
     */
    List<SlotDTO> getAvailableSlots(Long professionalId);

    /**
     * Crea i nuovi slot indicati per il professionista.
     */
    List<SlotDTO> createSlots(Long professionalId, List<SlotDTO> slots);

    /**
     * Elimina lo slot solo se il richiedente ne è il proprietario.
     */
    void deleteSlot(Long slotId, Long requesterId);

    /**
     * Genera gli slot dell'intervallo a partire dal calendario settimanale del professionista.
     */
    void generateSlotsFromSchedule(User professional, LocalDate startDate, LocalDate endDate);

    /**
     * Le prossime prenotazioni del professionista.
     */
    List<BookingResponse> getUpcomingBookings(Long professionalId);

    /**
     * Statistiche del professionista: prenotazioni, recensioni, crediti e simili.
     */
    ProfessionalStatsResponse getProfessionalStats(Long professionalId);
}

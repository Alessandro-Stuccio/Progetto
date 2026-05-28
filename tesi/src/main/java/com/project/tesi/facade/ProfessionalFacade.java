package com.project.tesi.facade;

import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.model.User;

import java.time.LocalDate;
import java.util.List;

public interface ProfessionalFacade {
    List<SlotDTO> getAvailableSlots(Long professionalId);
    List<SlotDTO> createSlots(Long professionalId, List<SlotDTO> slots);
    void deleteSlot(Long slotId, Long requesterId);
    void generateSlotsFromSchedule(User professional, LocalDate startDate, LocalDate endDate);
}

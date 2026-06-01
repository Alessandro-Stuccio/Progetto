package com.project.tesi.mapper;

import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte gli slot tra entità e DTO.
 */
@Component
public class SlotMapper {

    // isAvailable è true finché nessun cliente ha prenotato lo slot.
    public SlotDTO toDto(Slot slot) {
        if (slot == null) return null;
        return SlotDTO.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isAvailable(slot.getBookedBy() == null)
                .professionalId(slot.getProfessional().getId())
                .build();
    }

    public List<SlotDTO> toDtoList(List<Slot> slots) {
        return slots.stream().map(this::toDto).collect(Collectors.toList());
    }

    // Crea uno slot libero per il professionista: i campi di prenotazione restano vuoti.
    public Slot toEntity(SlotDTO dto, User professional) {
        return Slot.builder()
                .professional(professional)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    // Tutti gli slot della lista vengono assegnati allo stesso professionista.
    public List<Slot> toEntityList(List<SlotDTO> dtos, User professional) {
        return dtos.stream().map(dto -> toEntity(dto, professional)).collect(Collectors.toList());
    }
}

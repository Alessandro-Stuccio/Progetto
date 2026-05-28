package com.project.tesi.mapper;

import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper per la conversione tra {@link Slot} e {@link SlotDTO}.
 */
@Component
public class SlotMapper {

    /**
     * Converte uno {@link Slot} in {@link SlotDTO}.
     * Il campo {@code isAvailable} è {@code true} se lo slot non ha ancora un cliente prenotato.
     *
     * @param slot lo slot da convertire
     * @return il DTO, o {@code null} se lo slot è {@code null}
     */
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

    /**
     * Converte una lista di {@link Slot} in una lista di {@link SlotDTO}.
     *
     * @param slots lista degli slot
     * @return lista dei DTO
     */
    public List<SlotDTO> toDtoList(List<Slot> slots) {
        return slots.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Converte un {@link SlotDTO} in un'entità {@link Slot}, associando il professionista
     * proprietario. I campi di prenotazione non sono popolati (slot libero).
     *
     * @param dto          il DTO sorgente
     * @param professional l'utente professionista proprietario dello slot
     * @return l'entità {@link Slot}
     */
    public Slot toEntity(SlotDTO dto, User professional) {
        return Slot.builder()
                .professional(professional)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    /**
     * Converte una lista di {@link SlotDTO} in una lista di entità {@link Slot},
     * associando lo stesso professionista a tutti gli slot.
     *
     * @param dtos         lista dei DTO sorgente
     * @param professional il professionista proprietario
     * @return lista delle entità {@link Slot}
     */
    public List<Slot> toEntityList(List<SlotDTO> dtos, User professional) {
        return dtos.stream().map(dto -> toEntity(dto, professional)).collect(Collectors.toList());
    }
}

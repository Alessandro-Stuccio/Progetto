package com.project.tesi.mapper;

import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SlotMapper {

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

    public Slot toEntity(SlotDTO dto, User professional) {
        return Slot.builder()
                .professional(professional)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    public List<Slot> toEntityList(List<SlotDTO> dtos, User professional) {
        return dtos.stream().map(dto -> toEntity(dto, professional)).collect(Collectors.toList());
    }
}

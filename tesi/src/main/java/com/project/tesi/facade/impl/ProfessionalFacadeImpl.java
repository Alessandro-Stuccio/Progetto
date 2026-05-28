package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.facade.ProfessionalFacade;
import com.project.tesi.mapper.SlotMapper;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.model.WeeklySchedule;
import com.project.tesi.service.SlotService;
import com.project.tesi.service.UserService;
import com.project.tesi.service.WeeklyScheduleService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProfessionalFacadeImpl implements ProfessionalFacade {

    private final UserService userService;
    private final SlotService slotService;
    private final WeeklyScheduleService weeklyScheduleService;
    private final SlotMapper slotMapper;

    public ProfessionalFacadeImpl(UserService userService,
                                   SlotService slotService,
                                   WeeklyScheduleService weeklyScheduleService,
                                   SlotMapper slotMapper) {
        this.userService = userService;
        this.slotService = slotService;
        this.weeklyScheduleService = weeklyScheduleService;
        this.slotMapper = slotMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotDTO> getAvailableSlots(Long professionalId) {
        User professional = userService.getUserById(professionalId);
        return slotMapper.toDtoList(slotService.getAvailableSlots(professional));
    }

    @Override
    @Transactional
    public List<SlotDTO> createSlots(Long professionalId, List<SlotDTO> slots) {
        User professional = userService.getUserById(professionalId);
        if (professional.getRole() != Role.PERSONAL_TRAINER && professional.getRole() != Role.NUTRITIONIST) {
            throw new UnauthorizedAccessException("Solo i professionisti possono creare slot");
        }
        List<Slot> entities = slotMapper.toEntityList(slots, professional);
        return slotMapper.toDtoList(slotService.createSlots(entities));
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId, Long requesterId) {
        Slot slot = slotService.getSlot(slotId);
        if (!slot.getProfessional().getId().equals(requesterId)) {
            throw new UnauthorizedAccessException("Non sei autorizzato a eliminare questo slot");
        }
        if (slot.getBookedBy() != null || slot.getStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Non puoi eliminare uno slot già prenotato.");
        }
        slotService.deleteSlot(slotId);
    }

    @Override
    @Transactional
    public void generateSlotsFromSchedule(User professional, LocalDate startDate, LocalDate endDate) {
        if (professional.getRole() != Role.PERSONAL_TRAINER && professional.getRole() != Role.NUTRITIONIST) {
            throw new UnauthorizedAccessException("Solo i professionisti possono generare slot");
        }

        List<WeeklySchedule> schedules = weeklyScheduleService.findByProfessional(professional);
        List<Slot> newSlots = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDay = date;

            List<WeeklySchedule> dailyRules = schedules.stream()
                    .filter(s -> s.getDayOfWeek().equals(currentDay.getDayOfWeek()))
                    .toList();

            for (WeeklySchedule rule : dailyRules) {
                LocalTime currentTime = rule.getStartTime();

                while (currentTime.plusMinutes(30).isBefore(rule.getEndTime()) ||
                        currentTime.plusMinutes(30).equals(rule.getEndTime())) {

                    LocalDateTime startSlot = LocalDateTime.of(currentDay, currentTime);
                    LocalDateTime endSlot = startSlot.plusMinutes(30);

                    if (!slotService.slotExists(professional, startSlot)) {
                        newSlots.add(Slot.builder()
                                .professional(professional)
                                .startTime(startSlot)
                                .endTime(endSlot)
                                .bookedBy(null)
                                .build());
                    }

                    currentTime = currentTime.plusMinutes(30);
                }
            }
        }

        if (!newSlots.isEmpty()) {
            slotService.createSlots(newSlots);
        }
    }
}

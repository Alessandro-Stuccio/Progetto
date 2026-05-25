package com.project.tesi.service.impl;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.booking.SlotAlreadyBookedException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.model.WeeklySchedule;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.WeeklyScheduleRepository;
import com.project.tesi.service.SlotService;
import com.project.tesi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlotServiceImpl implements SlotService {

    private static final Logger log = LoggerFactory.getLogger(SlotServiceImpl.class);
    private final SlotRepository slotRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final UserService userService;

    public SlotServiceImpl(SlotRepository slotRepository,
                           WeeklyScheduleRepository weeklyScheduleRepository,
                           UserService userService) {
        this.slotRepository = slotRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public List<Slot> createSlots(List<Slot> slots) {
        return slotRepository.saveAll(slots);
    }

    @Override
    @Transactional
    public void generateSlotsFromSchedule(Long professionalId, LocalDate startDate, LocalDate endDate) {
        User professional = userService.getUserById(professionalId);

        if (professional.getRole() != Role.PERSONAL_TRAINER && professional.getRole() != Role.NUTRITIONIST) {
            throw new UnauthorizedAccessException("Solo i professionisti possono generare slot");
        }

        List<WeeklySchedule> schedules = weeklyScheduleRepository.findByProfessional(professional);
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

                    if (!slotRepository.existsByProfessionalAndStartTime(professional, startSlot)) {
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
            slotRepository.saveAll(newSlots);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Slot> getAvailableSlots(Long professionalId) {
        User professional = userService.getUserById(professionalId);
        return slotRepository.findByProfessionalAndBookedByIsNull(professional);
    }

    @Override
    @Transactional(readOnly = true)
    public Slot getSlot(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId));
    }

    @Override
    @Transactional
    public Slot saveBooking(Long slotId, User user, String meetingLink) {
        Slot slot = slotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId));

        if (slot.getBookedBy() != null) {
            throw new SlotAlreadyBookedException("Slot non più disponibile");
        }

        slot.setBookedBy(user);
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setMeetingLink(meetingLink);
        slot.setBookedAt(LocalDateTime.now());
        return slotRepository.save(slot);
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId) {
        if (!slotRepository.existsById(slotId)) {
            throw new ResourceNotFoundException("Slot", slotId);
        }
        slotRepository.deleteById(slotId);
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId, Long requesterId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId));
        if (!slot.getProfessional().getId().equals(requesterId)) {
            throw new UnauthorizedAccessException("Non sei autorizzato a eliminare questo slot");
        }
        slotRepository.deleteById(slotId);
    }

    @Override
    @Transactional
    public void cancelBooking(Long slotId, Long userId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione", slotId));
        slot.setBookedBy(null);
        slot.setStatus(BookingStatus.CANCELED);
        slot.setMeetingLink(null);
        slot.setBookedAt(null);
        slotRepository.save(slot);
    }

    @Override
    public List<Slot> findRecentByUser(User user, LocalDateTime since) {
        return slotRepository.findRecentByBookedBy(user, since);
    }

    @Override
    public List<Slot> findRecentByProfessional(User professional, LocalDateTime since) {
        return slotRepository.findRecentByProfessional(professional, since);
    }

    @Override
    public List<Slot> findBookingsByProfessional(User professional) {
        return slotRepository.findByProfessional(professional).stream()
                .filter(s -> s.getBookedBy() != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Slot> findFutureByUser(User user, LocalDateTime from) {
        return slotRepository.findFutureByBookedBy(user, from);
    }


}

package com.project.tesi.service.impl;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.exception.booking.SlotAlreadyBookedException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.model.WeeklySchedule;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.WeeklyScheduleRepository;
import com.project.tesi.service.SlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlotServiceImpl implements SlotService {

    private static final Logger log = LoggerFactory.getLogger(SlotServiceImpl.class);
    private final SlotRepository slotRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;

    public SlotServiceImpl(SlotRepository slotRepository,
                           WeeklyScheduleRepository weeklyScheduleRepository) {
        this.slotRepository = slotRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
    }

    @Override
    public List<Slot> createSlots(List<Slot> slots) {
        return slotRepository.saveAll(slots);
    }

    @Override
    public List<Slot> getAvailableSlots(User professional) {
        return slotRepository.findByProfessionalAndBookedByIsNull(professional);
    }

    @Override
    public Slot getSlot(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId));
    }

    @Override
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
    public void deleteSlot(Long slotId) {
        if (!slotRepository.existsById(slotId)) {
            throw new ResourceNotFoundException("Slot", slotId);
        }
        slotRepository.deleteById(slotId);
    }

    @Override
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

    @Override
    public boolean slotExists(User professional, LocalDateTime startTime) {
        return slotRepository.existsByProfessionalAndStartTime(professional, startTime);
    }

    @Override
    public List<WeeklySchedule> getSchedulesByProfessional(User professional) {
        return weeklyScheduleRepository.findByProfessional(professional);
    }

    @Override
    public void clearBookingsByUser(Long userId) {
        slotRepository.clearBookingByUserId(userId);
    }

    @Override
    public void deleteSlotsByProfessional(Long professionalId) {
        slotRepository.deleteByProfessionalId(professionalId);
    }

    @Override
    public void deleteSchedulesByProfessional(Long professionalId) {
        weeklyScheduleRepository.deleteByProfessionalId(professionalId);
    }
}

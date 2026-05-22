package com.project.tesi.service.impl;

import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.model.WeeklySchedule;
import com.project.tesi.mapper.SlotMapper;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.repository.WeeklyScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceImplTest {

    @Mock private SlotRepository slotRepository;
    @Mock private UserRepository userRepository;
    @Mock private WeeklyScheduleRepository weeklyScheduleRepository;
    @Mock private SlotMapper slotMapper;

    @InjectMocks private SlotServiceImpl slotService;

    private User pt;

    @BeforeEach
    void setUp() {
        pt = User.builder().email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).id(2L).firstName("Luca").lastName("Bianchi").build();
    }

    @Test @DisplayName("createSlots — crea e salva slot")
    void createSlots_success() {
        SlotDTO dto = SlotDTO.builder()
                .startTime(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endTime(LocalDateTime.of(2026, 3, 15, 10, 30)).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));
        Slot saved = Slot.builder().id(1L).professional(pt)
                .startTime(dto.getStartTime()).endTime(dto.getEndTime()).build();
        when(slotRepository.saveAll(anyList())).thenReturn(List.of(saved));
        when(slotMapper.toDto(saved)).thenReturn(
                SlotDTO.builder().id(1L).startTime(dto.getStartTime()).endTime(dto.getEndTime()).isAvailable(true).professionalId(2L).build());

        List<SlotDTO> result = slotService.createSlots(2L, List.of(dto));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isAvailable()).isTrue();
    }

    @Test @DisplayName("createSlots — professionista non trovato")
    void createSlots_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.createSlots(999L, List.of())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test @DisplayName("getAvailableSlots — restituisce slot disponibili")
    void getAvailableSlots_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));
        Slot s = Slot.builder().id(1L).professional(pt)
                .startTime(LocalDateTime.now().plusDays(1)).endTime(LocalDateTime.now().plusDays(1).plusMinutes(30))
                .build();
        when(slotRepository.findByProfessionalAndBookedByIsNull(pt)).thenReturn(List.of(s));
        when(slotMapper.toDto(s)).thenReturn(SlotDTO.builder().id(1L).isAvailable(true).professionalId(2L).build());

        List<SlotDTO> result = slotService.getAvailableSlots(2L);
        assertThat(result).hasSize(1);
    }

    @Test @DisplayName("getAvailableSlots — professionista non trovato")
    void getAvailableSlots_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.getAvailableSlots(999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test @DisplayName("deleteSlot — chiama deleteById")
    void deleteSlot() {
        slotService.deleteSlot(10L);
        verify(slotRepository).deleteById(10L);
    }

    @Test @DisplayName("generateSlotsFromSchedule — genera slot da orario settimanale")
    void generateSlotsFromSchedule_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));

        LocalDate nextMonday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
        WeeklySchedule schedule = WeeklySchedule.builder()
                .professional(pt).dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0)).build();
        when(weeklyScheduleRepository.findByProfessional(pt)).thenReturn(List.of(schedule));
        when(slotRepository.existsByProfessionalAndStartTime(any(), any())).thenReturn(false);

        slotService.generateSlotsFromSchedule(2L, nextMonday, nextMonday);

        verify(slotRepository).saveAll(argThat(list -> ((List<?>) list).size() == 2));
    }

    @Test @DisplayName("generateSlotsFromSchedule — slot già esistente non viene duplicato")
    void generateSlotsFromSchedule_noDuplicates() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));
        LocalDate nextMon = LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
        WeeklySchedule schedule = WeeklySchedule.builder()
                .professional(pt).dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();
        when(weeklyScheduleRepository.findByProfessional(pt)).thenReturn(List.of(schedule));
        when(slotRepository.existsByProfessionalAndStartTime(any(), any())).thenReturn(true);

        slotService.generateSlotsFromSchedule(2L, nextMon, nextMon);

        verify(slotRepository, never()).saveAll(any());
    }
}

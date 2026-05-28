package com.project.tesi.service.impl;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.booking.BookingCancellationException;
import com.project.tesi.exception.booking.SlotAlreadyBookedException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceImplTest {

    @Mock private SlotRepository slotRepository;

    private SlotServiceImpl slotService;

    private User client;
    private User pt;
    private Slot slot;

    @BeforeEach
    void setUp() {
        pt = User.builder().email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).id(2L).firstName("Luca").lastName("Bianchi").build();
        client = User.builder().email("mario@test.com").password("testpass").role(Role.CLIENT).id(1L).firstName("Mario").lastName("Rossi").assignedPT(pt).build();

        slot = Slot.builder().id(10L).professional(pt)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusMinutes(30))
                .build();

        slotService = new SlotServiceImpl(slotRepository);
    }

    // ─── createSlots ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("createSlots — salva gli slot e li restituisce")
    void createSlots_success() {
        Slot input = Slot.builder()
                .professional(pt)
                .startTime(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endTime(LocalDateTime.of(2026, 3, 15, 10, 30))
                .build();
        Slot saved = Slot.builder().id(1L).professional(pt)
                .startTime(input.getStartTime()).endTime(input.getEndTime()).build();

        when(slotRepository.saveAll(anyList())).thenReturn(List.of(saved));

        List<Slot> result = slotService.createSlots(List.of(input));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProfessional()).isEqualTo(pt);
    }

    // ─── getAvailableSlots ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAvailableSlots — restituisce slot disponibili")
    void getAvailableSlots_success() {
        when(slotRepository.findByProfessionalAndBookedByIsNull(pt)).thenReturn(List.of(slot));

        List<Slot> result = slotService.getAvailableSlots(pt);
        assertThat(result).hasSize(1);
    }

    // ─── getSlot ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getSlot — restituisce slot esistente")
    void getSlot_success() {
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));
        assertThat(slotService.getSlot(10L)).isEqualTo(slot);
    }

    @Test
    @DisplayName("getSlot — slot non trovato lancia ResourceNotFoundException")
    void getSlot_notFound() {
        when(slotRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.getSlot(999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── saveBooking ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveBooking — salva prenotazione con campi corretti")
    void saveBooking_success() {
        when(slotRepository.findByIdWithLock(10L)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Slot result = slotService.saveBooking(10L, client, "https://meet.jit.si/test");

        assertThat(result.getBookedBy()).isEqualTo(client);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(result.getMeetingLink()).isEqualTo("https://meet.jit.si/test");
        assertThat(result.getBookedAt()).isNotNull();
    }

    @Test
    @DisplayName("saveBooking — slot già prenotato lancia SlotAlreadyBookedException")
    void saveBooking_alreadyBooked() {
        slot.setBookedBy(client);
        when(slotRepository.findByIdWithLock(10L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> slotService.saveBooking(10L, client, "link"))
                .isInstanceOf(SlotAlreadyBookedException.class);
    }

    @Test
    @DisplayName("saveBooking — slot non trovato lancia ResourceNotFoundException")
    void saveBooking_slotNotFound() {
        when(slotRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.saveBooking(999L, client, "link"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── cancelBooking ────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelBooking — annulla prenotazione con successo")
    void cancelBooking_success() {
        slot.setBookedBy(client);
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setMeetingLink("https://meet.jit.si/test");
        slot.setBookedAt(LocalDateTime.now().minusDays(1));

        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        slotService.cancelBooking(10L, 1L);

        assertThat(slot.getBookedBy()).isNull();
        assertThat(slot.getStatus()).isEqualTo(BookingStatus.CANCELED);
        assertThat(slot.getMeetingLink()).isNull();
        verify(slotRepository).save(slot);
    }

    // ─── deleteSlot ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteSlot — chiama deleteById (singolo argomento)")
    void deleteSlot() {
        when(slotRepository.existsById(10L)).thenReturn(true);
        slotService.deleteSlot(10L);
        verify(slotRepository).deleteById(10L);
    }

}

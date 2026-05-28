package com.project.tesi.facade;

import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.facade.impl.ProfessionalFacadeImpl;
import com.project.tesi.mapper.SlotMapper;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.service.SlotService;
import com.project.tesi.service.UserService;
import com.project.tesi.service.WeeklyScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalFacadeTest {

    @Mock private UserService userService;
    @Mock private SlotService slotService;
    @Mock private WeeklyScheduleService weeklyScheduleService;
    @Mock private SlotMapper slotMapper;

    private ProfessionalFacadeImpl professionalFacade;

    @BeforeEach
    void setUp() {
        professionalFacade = new ProfessionalFacadeImpl(userService, slotService, weeklyScheduleService, slotMapper);
    }

    private User buildUser(Long id, Role role) {
        return User.builder().id(id).email(role.name().toLowerCase() + id + "@test.com")
                .password("password123").firstName("Test").lastName("User").role(role).build();
    }

    @Test
    @DisplayName("getAvailableSlots — delega al SlotService e mappa via SlotMapper")
    void getAvailableSlots() {
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        Slot slot = Slot.builder().id(1L).professional(pt)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30)).build();
        SlotDTO dto = SlotDTO.builder().id(1L).professionalId(2L).isAvailable(true).build();

        when(userService.getUserById(2L)).thenReturn(pt);
        when(slotService.getAvailableSlots(pt)).thenReturn(List.of(slot));
        when(slotMapper.toDtoList(anyList())).thenReturn(List.of(dto));

        List<SlotDTO> result = professionalFacade.getAvailableSlots(2L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("createSlots — PT valido: crea slot e mappa a DTO")
    void createSlots_success() {
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        SlotDTO inputDto = SlotDTO.builder()
                .startTime(LocalDateTime.of(2026, 6, 1, 10, 0))
                .endTime(LocalDateTime.of(2026, 6, 1, 10, 30)).build();
        Slot saved = Slot.builder().id(1L).professional(pt)
                .startTime(inputDto.getStartTime()).endTime(inputDto.getEndTime()).build();
        SlotDTO outputDto = SlotDTO.builder().id(1L).professionalId(2L).isAvailable(true).build();

        when(userService.getUserById(2L)).thenReturn(pt);
        when(slotService.createSlots(anyList())).thenReturn(List.of(saved));
        when(slotMapper.toDtoList(anyList())).thenReturn(List.of(outputDto));

        List<SlotDTO> result = professionalFacade.createSlots(2L, List.of(inputDto));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("createSlots — utente CLIENT: lancia UnauthorizedAccessException")
    void createSlots_notProfessional() {
        User client = buildUser(1L, Role.CLIENT);
        when(userService.getUserById(1L)).thenReturn(client);

        assertThatThrownBy(() -> professionalFacade.createSlots(1L, List.of()))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("deleteSlot — professionista autorizzato: chiama deleteSlot singolo argomento")
    void deleteSlot_authorized() {
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        Slot slot = Slot.builder().id(10L).professional(pt)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30)).build();

        when(slotService.getSlot(10L)).thenReturn(slot);

        professionalFacade.deleteSlot(10L, 2L);

        verify(slotService).deleteSlot(10L);
    }

    @Test
    @DisplayName("deleteSlot — requesterId diverso dal professionista: lancia UnauthorizedAccessException")
    void deleteSlot_unauthorized() {
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        Slot slot = Slot.builder().id(10L).professional(pt)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30)).build();

        when(slotService.getSlot(10L)).thenReturn(slot);

        assertThatThrownBy(() -> professionalFacade.deleteSlot(10L, 99L))
                .isInstanceOf(UnauthorizedAccessException.class);
        verify(slotService, never()).deleteSlot(anyLong());
    }
}

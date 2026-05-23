package com.project.tesi.mapper;

import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    private final BookingMapper mapper = new BookingMapper();

    @Test
    @DisplayName("toResponse — converte correttamente uno Slot nel DTO")
    void toResponse_success() {
        User client = User.builder().email("test@test.com").password("testpass").role(Role.CLIENT).id(1L).firstName("Mario").lastName("Rossi").build();
        User pt = User.builder().email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).id(2L).firstName("Luca").lastName("Bianchi").build();

        // Orario nel futuro prossimo (entro la finestra di joinability -10/+30 min)
        LocalDateTime start = LocalDateTime.now().plusMinutes(5).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        Slot slot = Slot.builder().id(1L).professional(pt).startTime(start).endTime(end).build();
        slot.setBookedBy(client);
        slot.setMeetingLink("https://meet.jit.si/test");
        slot.setStatus(BookingStatus.CONFIRMED);

        BookingResponse response = mapper.toResponse(slot);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDate()).isEqualTo(start.format(dateFmt));
        assertThat(response.getStartTime()).isEqualTo(start.format(timeFmt));
        assertThat(response.getEndTime()).isEqualTo(end.format(timeFmt));
        assertThat(response.getProfessionalName()).isEqualTo("Luca Bianchi");
        assertThat(response.getClientName()).isEqualTo("Mario Rossi");
        assertThat(response.getProfessionalRole()).isEqualTo(Role.PERSONAL_TRAINER);
        assertThat(response.getMeetingLink()).isEqualTo("https://meet.jit.si/test");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.isCanJoin()).isTrue();
    }

    @Test
    @DisplayName("toResponse — null input restituisce null")
    void toResponse_null() {
        assertThat(mapper.toResponse(null)).isNull();
    }
}

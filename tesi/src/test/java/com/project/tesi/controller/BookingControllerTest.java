package com.project.tesi.controller;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.UserFacade;
import com.project.tesi.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test unitari per {@link BookingController}.
 */
@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock private UserFacade userFacade;

    @InjectMocks
    private BookingController bookingController;

    @Test
    @DisplayName("createBooking — restituisce 200 con la prenotazione")
    void createBooking() {
        User mockUser = User.builder().id(1L).email("test@test.com").password("testpass").role(Role.CLIENT).build();
        BookingRequest req = new BookingRequest(10L);
        BookingResponse resp = BookingResponse.builder().id(1L).status(BookingStatus.CONFIRMED).build();
        when(userFacade.createBooking(req, 1L)).thenReturn(resp);

        ResponseEntity<BookingResponse> response = bookingController.createBooking(req, mockUser);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }
}

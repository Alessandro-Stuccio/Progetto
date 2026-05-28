package com.project.tesi.facade;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;

public interface BookingFacade {
    BookingResponse createBooking(BookingRequest request, Long userId);
    void cancelBooking(Long bookingId, Long userId);
}

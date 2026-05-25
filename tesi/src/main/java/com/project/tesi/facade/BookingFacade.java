package com.project.tesi.facade;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.dto.response.SlotDTO;

import java.util.List;

public interface BookingFacade {
    BookingResponse createBooking(BookingRequest request, Long userId);
    void cancelBooking(Long bookingId, Long userId);
    List<SlotDTO> getAvailableSlots(Long professionalId);
    List<SlotDTO> createSlots(Long professionalId, List<SlotDTO> slots);
    void deleteSlot(Long slotId, Long requesterId);
}

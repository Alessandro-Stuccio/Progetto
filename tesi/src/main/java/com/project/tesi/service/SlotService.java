package com.project.tesi.service;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Validated
public interface SlotService {

    List<SlotDTO> createSlots(@NotNull @Min(1) Long professionalId, @NotNull @NotEmpty List<SlotDTO> slotsDTO);

    List<SlotDTO> getAvailableSlots(@NotNull @Min(1) Long professionalId);

    void deleteSlot(@NotNull @Min(1) Long slotId);

    void deleteSlot(@NotNull @Min(1) Long slotId, @NotNull @Min(1) Long requesterId);

    void generateSlotsFromSchedule(@NotNull @Min(1) Long professionalId,
                                   @NotNull LocalDate startDate,
                                   @NotNull LocalDate endDate);

    BookingResponse createBooking(@NotNull @Valid BookingRequest request, @NotNull @Min(1) Long userId);

    void cancelBooking(@NotNull @Min(1) Long slotId, @NotNull @Min(1) Long userId);

    List<Slot> findRecentByUser(@NotNull User user, @NotNull LocalDateTime since);

    List<Slot> findRecentByProfessional(@NotNull User professional, @NotNull LocalDateTime since);

    List<Slot> findBookingsByProfessional(@NotNull User professional);

    List<Slot> findFutureByUser(@NotNull User user, @NotNull LocalDateTime from);
}

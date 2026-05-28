package com.project.tesi.service;

import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Validated
public interface SlotService {

    List<Slot> createSlots(@NotNull @NotEmpty List<Slot> slots);

    List<Slot> getAvailableSlots(@NotNull User professional);

    Slot getSlot(@NotNull @Min(1) Long slotId);

    Slot saveBooking(@NotNull @Min(1) Long slotId, @NotNull User user, @NotBlank String meetingLink);

    void deleteSlot(@NotNull @Min(1) Long slotId);

    void cancelBooking(@NotNull @Min(1) Long slotId, @NotNull @Min(1) Long userId);

    boolean slotExists(@NotNull User professional, @NotNull LocalDateTime startTime);

    List<Slot> findRecentByUser(@NotNull User user, @NotNull LocalDateTime since);

    List<Slot> findRecentByProfessional(@NotNull User professional, @NotNull LocalDateTime since);

    List<Slot> findBookingsByProfessional(@NotNull User professional);

    List<Slot> findFutureByUser(@NotNull User user, @NotNull LocalDateTime from);

    List<Slot> getAllBookedSlots();

    void logBookingCreated(@NotNull Slot slot);

    List<Slot> findTodayByProfessional(@NotNull User professional,
                                       @NotNull LocalDateTime dayStart,
                                       @NotNull LocalDateTime dayEnd);

    boolean hasBookingBetween(@NotNull @Min(1) Long clientId, @NotNull @Min(1) Long professionalId);
}

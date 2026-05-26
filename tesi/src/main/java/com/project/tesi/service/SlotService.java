package com.project.tesi.service;

import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.model.WeeklySchedule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    List<WeeklySchedule> getSchedulesByProfessional(@NotNull User professional);

    List<Slot> findRecentByUser(@NotNull User user, @NotNull LocalDateTime since);

    List<Slot> findRecentByProfessional(@NotNull User professional, @NotNull LocalDateTime since);

    List<Slot> findBookingsByProfessional(@NotNull User professional);

    List<Slot> findFutureByUser(@NotNull User user, @NotNull LocalDateTime from);

    void clearBookingsByUser(@NotNull @Min(1) Long userId);

    void deleteSlotsByProfessional(@NotNull @Min(1) Long professionalId);

    void deleteSchedulesByProfessional(@NotNull @Min(1) Long professionalId);
}

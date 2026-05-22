package com.project.tesi.builder;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.model.Booking;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;

@Component
public class BookingDirector {

    public Booking buildConfirmedBooking(User user, Slot slot) {
        slot.setStatus(BookingStatus.CONFIRMED);
        return Booking.builder()
                .user(user)
                .slot(slot)
                .build();
    }

    public Booking buildCompletedBooking(User user, Slot slot) {
        slot.setStatus(BookingStatus.COMPLETED);
        return Booking.builder()
                .user(user)
                .slot(slot)
                .build();
    }

    public Booking buildCancelledBooking(User user, Slot slot) {
        slot.setStatus(BookingStatus.CANCELED);
        return Booking.builder()
                .user(user)
                .slot(slot)
                .build();
    }
}

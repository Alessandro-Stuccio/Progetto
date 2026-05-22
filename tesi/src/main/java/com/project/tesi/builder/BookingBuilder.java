package com.project.tesi.builder;

import java.time.LocalDateTime;
import com.project.tesi.model.*;


public interface BookingBuilder {
    BookingBuilder id(Long id);
    BookingBuilder user(User user);
    BookingBuilder slot(Slot slot);
    BookingBuilder bookedAt(LocalDateTime bookedAt);
    Booking build();
}

package com.project.tesi.builder.impl;

import com.project.tesi.builder.BookingBuilder;
import com.project.tesi.model.Booking;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;

import java.time.LocalDateTime;
import java.util.Objects;

public class BookingBuilderImpl implements BookingBuilder {

    private Long id;
    private User user;
    private Slot slot;
    private LocalDateTime bookedAt;

    @Override
    public BookingBuilder id(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public BookingBuilder user(User user) {
        this.user = user;
        return this;
    }

    @Override
    public BookingBuilder slot(Slot slot) {
        this.slot = slot;
        return this;
    }

    @Override
    public BookingBuilder bookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
        return this;
    }

    @Override
    public Booking build() {
        Objects.requireNonNull(this.user, "user è obbligatorio");
        Objects.requireNonNull(this.slot, "slot è obbligatorio");
        Objects.requireNonNull(this.slot.getProfessional(), "slot.professional è obbligatorio");

        if (this.user.getId() != null && this.slot.getProfessional().getId() != null
                && this.user.getId().equals(this.slot.getProfessional().getId()))
            throw new IllegalStateException("user e professional non possono essere lo stesso utente");

        Booking obj = new Booking();
        obj.setId(this.id);
        obj.setUser(this.user);
        obj.setSlot(this.slot);
        if (this.bookedAt != null) {
            obj.setBookedAt(this.bookedAt);
        }
        return obj;
    }
}

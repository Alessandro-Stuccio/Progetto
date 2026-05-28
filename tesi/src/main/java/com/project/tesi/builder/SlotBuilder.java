package com.project.tesi.builder;

import com.project.tesi.model.Slot;
import com.project.tesi.model.User;

import java.time.LocalDateTime;

/**
 * Builder per l'entità Slot. Fluent interface per la costruzione di uno slot di disponibilità professionale.
 */
public interface SlotBuilder {
    SlotBuilder id(Long id);
    SlotBuilder professional(User professional);
    SlotBuilder startTime(LocalDateTime startTime);
    SlotBuilder endTime(LocalDateTime endTime);
    SlotBuilder bookedBy(User bookedBy);
    SlotBuilder version(Integer version);
    SlotBuilder bookedAt(LocalDateTime bookedAt);
    Slot build();
}

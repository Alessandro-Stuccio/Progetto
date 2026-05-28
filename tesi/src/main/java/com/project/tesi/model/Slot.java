package com.project.tesi.model;

import com.project.tesi.builder.SlotBuilder;
import com.project.tesi.builder.impl.SlotBuilderImpl;
import com.project.tesi.enums.BookingStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entità JPA per uno slot di disponibilità di un professionista.
 * La durata è sempre 30 minuti (startTime → endTime). Quando prenotato,
 * {@code bookedBy} e {@code status} vengono popolati. Il campo {@code version}
 * supporta l'optimistic locking per prevenire il double-booking. Il campo
 * {@code reminderSent} evita l'invio duplicato del promemoria email.
 */
@Entity
@Table(
    name = "slots",
    indexes = {
        @Index(name = "idx_slot_time", columnList = "startTime"),
        @Index(name = "idx_slot_prof", columnList = "professional_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_slot_prof_start", columnNames = {"professional_id", "startTime"})
    }
)
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Professionista proprietario dello slot. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false, foreignKey = @ForeignKey(name = "fk_slot_professional_id"))
    private User professional;

    /** Inizio dello slot; distanza fissa di 30 minuti da {@code endTime}. */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /** Fine dello slot; distanza fissa di 30 minuti da {@code startTime}. */
    @Column(nullable = false)
    private LocalDateTime endTime;

    /** Cliente che ha prenotato lo slot; {@code null} se lo slot è libero. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booked_by_id", foreignKey = @ForeignKey(name = "fk_slot_booked_by_id"))
    private User bookedBy;

    /** Stato corrente della prenotazione (es. AVAILABLE, BOOKED, COMPLETED). */
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    /** Link Jitsi generato al momento della prenotazione; {@code null} se non prenotato. */
    @Column
    private String meetingLink;

    /** Flag che indica se il promemoria email è già stato inviato per questo slot. */
    private boolean reminderSent = false;

    /** Versione per l'optimistic locking; incrementata automaticamente da JPA ad ogni aggiornamento. */
    @Version
    private Integer version;

    /** Timestamp del momento in cui lo slot è stato prenotato. */
    @Column(name = "booked_at")
    private LocalDateTime bookedAt;

    public Slot() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getProfessional() { return professional; }
    public void setProfessional(User professional) { this.professional = professional; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public User getBookedBy() { return bookedBy; }
    public void setBookedBy(User bookedBy) { this.bookedBy = bookedBy; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }

    public static SlotBuilder builder() {
        return new SlotBuilderImpl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slot that = (Slot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Slot{id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", status=" + status + "}";
    }
}

package com.project.tesi.model;

import com.project.tesi.builder.WeeklyScheduleBuilder;
import com.project.tesi.builder.impl.WeeklyScheduleBuilderImpl;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Entità JPA per la disponibilità settimanale ricorrente di un professionista.
 * Vincolo unico su (professional_id, dayOfWeek): un solo slot orario per giorno
 * per professionista. Usata dallo {@code SlotGenerationScheduler} per creare
 * automaticamente gli slot futuri.
 */
@Entity
@Table(name = "weekly_schedules", uniqueConstraints = {
        @UniqueConstraint(name = "uq_weekly_schedule_prof_day", columnNames = {"professional_id", "dayOfWeek"})
})
public class WeeklySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Professionista a cui appartiene questa fascia oraria settimanale. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false, foreignKey = @ForeignKey(name = "fk_weekly_schedule_professional_id"))
    private User professional;

    /** Giorno della settimana a cui si applica la disponibilità (es. MONDAY, TUESDAY). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    /** Orario di inizio della disponibilità nel giorno indicato. */
    @Column(nullable = false)
    private LocalTime startTime;

    /** Orario di fine della disponibilità nel giorno indicato. */
    @Column(nullable = false)
    private LocalTime endTime;

    public WeeklySchedule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getProfessional() { return professional; }
    public void setProfessional(User professional) { this.professional = professional; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public static WeeklyScheduleBuilder builder() {
        return new WeeklyScheduleBuilderImpl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeeklySchedule that = (WeeklySchedule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WeeklySchedule{id=" + id + ", dayOfWeek=" + dayOfWeek + ", startTime=" + startTime + ", endTime=" + endTime + "}";
    }
}

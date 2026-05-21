package com.project.tesi.model;

import com.project.tesi.builder.SlotBuilder;
import com.project.tesi.builder.impl.SlotBuilderImpl;
import com.project.tesi.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

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
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"professional", "bookedBy"})
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false, foreignKey = @ForeignKey(name = "fk_slot_professional_id"))
    private User professional;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booked_by_id", foreignKey = @ForeignKey(name = "fk_slot_booked_by_id"))
    private User bookedBy;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(nullable = false)
    private String meetingLink;

    private boolean reminderSent = false;

    @Version
    private Integer version;

    public static SlotBuilder builder() {
        return new SlotBuilderImpl();
    }
}

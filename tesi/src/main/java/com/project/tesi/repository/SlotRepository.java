package com.project.tesi.repository;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    boolean existsByProfessionalAndStartTime(User professional, LocalDateTime startTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT s FROM Slot s WHERE s.professional.id = :profId " +
            "AND s.bookedBy IS NULL " +
            "AND s.startTime BETWEEN :start AND :end " +
            "ORDER BY s.startTime ASC")
    List<Slot> findAvailableSlots(@Param("profId") Long profId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<Slot> findByProfessionalAndBookedByIsNull(User professional);

    List<Slot> findByProfessional(User professional);

    // ---- Query ex-BookingRepository ----

    List<Slot> findByBookedBy(User bookedBy);

    @Query("SELECT s FROM Slot s WHERE s.bookedBy = :user AND s.startTime > :now ORDER BY s.startTime ASC")
    List<Slot> findFutureByBookedBy(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Slot s WHERE s.professional = :professional " +
           "AND s.startTime >= :dayStart AND s.startTime < :dayEnd " +
           "AND s.bookedBy IS NOT NULL " +
           "ORDER BY s.startTime ASC")
    List<Slot> findTodayByProfessional(@Param("professional") User professional,
                                        @Param("dayStart") LocalDateTime dayStart,
                                        @Param("dayEnd") LocalDateTime dayEnd);

    @Query("SELECT s FROM Slot s WHERE s.bookedBy = :user AND s.bookedAt >= :since ORDER BY s.bookedAt DESC")
    List<Slot> findRecentByBookedBy(@Param("user") User user, @Param("since") LocalDateTime since);

    @Query("SELECT s FROM Slot s WHERE s.professional = :professional AND s.bookedAt >= :since ORDER BY s.bookedAt DESC")
    List<Slot> findRecentByProfessional(@Param("professional") User professional, @Param("since") LocalDateTime since);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Slot s " +
           "WHERE s.bookedBy.id = :userId AND s.professional.id = :professionalId")
    boolean existsByBookedByIdAndProfessionalId(@Param("userId") Long userId,
                                                 @Param("professionalId") Long professionalId);

    @Query("SELECT s FROM Slot s " +
           "WHERE s.status = com.project.tesi.enums.BookingStatus.CONFIRMED " +
           "AND s.reminderSent = false " +
           "AND s.bookedBy IS NOT NULL " +
           "AND s.startTime >= :from " +
           "AND s.startTime <= :to")
    List<Slot> findUpcomingNeedingReminder(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    @Query("SELECT s FROM Slot s WHERE s.bookedBy IS NOT NULL AND s.bookedAt IS NOT NULL")
    List<Slot> findAllBooked();

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Slot s " +
           "WHERE s.id = :slotId AND s.status = :status")
    boolean existsByIdAndStatus(@Param("slotId") Long slotId, @Param("status") BookingStatus status);
}

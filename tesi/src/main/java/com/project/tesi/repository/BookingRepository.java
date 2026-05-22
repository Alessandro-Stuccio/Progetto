package com.project.tesi.repository;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.model.Booking;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);

    @Query("SELECT b FROM Booking b WHERE b.slot.professional = :professional")
    List<Booking> findByProfessional(@Param("professional") User professional);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.user.id = :userId AND b.slot.professional.id = :professionalId")
    boolean existsByUserIdAndProfessionalId(@Param("userId") Long userId, @Param("professionalId") Long professionalId);

    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.slot.startTime > :now ORDER BY b.slot.startTime ASC")
    List<Booking> findFutureByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.slot.professional = :professional AND b.slot.startTime >= :dayStart AND b.slot.startTime < :dayEnd ORDER BY b.slot.startTime ASC")
    List<Booking> findTodayByProfessional(@Param("professional") User professional, @Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);

    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.bookedAt >= :since ORDER BY b.bookedAt DESC")
    List<Booking> findRecentByUser(@Param("user") User user, @Param("since") LocalDateTime since);

    @Query("SELECT b FROM Booking b WHERE b.slot.professional = :professional AND b.bookedAt >= :since ORDER BY b.bookedAt DESC")
    List<Booking> findRecentByProfessional(@Param("professional") User professional, @Param("since") LocalDateTime since);

    boolean existsBySlotAndSlotStatus(Slot slot, BookingStatus status);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.slot = :slot AND b.slot.status = :status")
    void deleteBySlotAndStatus(@Param("slot") Slot slot, @Param("status") BookingStatus status);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.user.id = :userId OR b.slot.professional.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.slot.status = com.project.tesi.enums.BookingStatus.CONFIRMED " +
           "AND b.slot.reminderSent = false " +
           "AND b.slot.startTime >= :from " +
           "AND b.slot.startTime <= :to")
    List<Booking> findUpcomingNeedingReminder(@Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);
}

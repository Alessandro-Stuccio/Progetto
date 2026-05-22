package com.project.tesi.service.impl;

import com.project.tesi.model.Booking;
import com.project.tesi.repository.BookingRepository;
import com.project.tesi.service.ActivityFeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ActivityFeedServiceImpl implements ActivityFeedService {

    private static final Logger log = LoggerFactory.getLogger(ActivityFeedServiceImpl.class);

    private final BookingRepository bookingRepository;

    public ActivityFeedServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public void logBookingCreated(Booking booking) {
        if (booking.getBookedAt() == null) {
            booking.setBookedAt(LocalDateTime.now());
            bookingRepository.save(booking);
            log.info("ActivityFeed [Observer]: timestamp bookedAt registrato per prenotazione ID={}", booking.getId());
        } else {
            log.info("ActivityFeed [Observer]: prenotazione ID={} già registrata (bookedAt={}).",
                    booking.getId(), booking.getBookedAt());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async("emailTaskExecutor")
    public void logDocumentUploaded(Long clientId, Long uploaderId, String type) {
        log.info("ActivityFeed: upload documento tipo={} per clientId={} da uploaderId={}", type, clientId, uploaderId);
    }
}

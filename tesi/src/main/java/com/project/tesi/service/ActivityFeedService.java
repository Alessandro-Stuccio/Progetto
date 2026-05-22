package com.project.tesi.service;

import com.project.tesi.model.Booking;
import org.springframework.scheduling.annotation.Async;

public interface ActivityFeedService {

    void logBookingCreated(Booking booking);

    @Async("emailTaskExecutor")
    void logDocumentUploaded(Long clientId, Long uploaderId, String type);
}

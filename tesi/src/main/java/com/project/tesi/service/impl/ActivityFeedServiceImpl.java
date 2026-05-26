package com.project.tesi.service.impl;

import com.project.tesi.model.Slot;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.service.ActivityFeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ActivityFeedServiceImpl implements ActivityFeedService {

    private static final Logger log = LoggerFactory.getLogger(ActivityFeedServiceImpl.class);

    private final SlotRepository slotRepository;

    public ActivityFeedServiceImpl(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Override
    public void logBookingCreated(Slot slot) {
        if (slot.getBookedAt() == null) {
            slot.setBookedAt(LocalDateTime.now());
            slotRepository.save(slot);
            log.info("ActivityFeed [Observer]: timestamp bookedAt registrato per slot ID={}", slot.getId());
        } else {
            log.info("ActivityFeed [Observer]: slot ID={} già registrato (bookedAt={}).", slot.getId(), slot.getBookedAt());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async("emailTaskExecutor")
    public void logDocumentUploaded(Long clientId, Long uploaderId, String type) {
        log.info("ActivityFeed: upload documento tipo={} per clientId={} da uploaderId={}", type, clientId, uploaderId);
    }
}

package com.project.tesi.service;

import com.project.tesi.dto.response.ActivityFeedItemResponse;
import com.project.tesi.model.Booking;
import com.project.tesi.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Validated
public interface ActivityFeedService {

    List<ActivityFeedItemResponse> getActivityFeed(@NotNull Long userId, int days, int limit);


    /** Chiamato in modo sincrono dentro la transazione di creazione prenotazione. */
    void logBookingCreated(@Valid Booking booking);

    /** Eseguito in modo asincrono su emailTaskExecutor dopo l'upload di un documento. */
    @Async("emailTaskExecutor")
    void logDocumentUploaded(Long clientId, Long uploaderId, String type);
}

package com.project.tesi.service;

import com.project.tesi.model.Slot;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ActivityFeedService {

    void logBookingCreated(@NotNull Slot slot);

    @Async("emailTaskExecutor")
    void logDocumentUploaded(@NotNull @Min(1) Long clientId,
                             @NotNull @Min(1) Long uploaderId,
                             @NotBlank String type);
}

package com.project.tesi.service;

import com.project.tesi.enums.DocumentType;
import com.project.tesi.model.Document;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Validated
public interface ProfessionalStatsService {
    List<Slot> getTodaySlots(@NotNull User professional, @NotNull LocalDateTime dayStart, @NotNull LocalDateTime dayEnd);
    List<User> getAssignedClients(@NotNull User professional);
    Document getLatestDocumentByOwnerAndType(@NotNull User owner, @NotNull DocumentType type);
    int countDocumentsUploadedSince(@NotNull User professional, @NotNull LocalDateTime since);
}

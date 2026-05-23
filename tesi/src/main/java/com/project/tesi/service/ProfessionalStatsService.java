package com.project.tesi.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;

@Validated
public interface ProfessionalStatsService {

    ProfessionalStatsResponse getProfessionalStats(@NotNull @Min(1) Long professionalId);
}

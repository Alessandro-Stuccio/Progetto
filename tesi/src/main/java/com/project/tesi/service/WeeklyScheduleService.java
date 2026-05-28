package com.project.tesi.service;

import com.project.tesi.model.User;
import com.project.tesi.model.WeeklySchedule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface WeeklyScheduleService {

    List<WeeklySchedule> findByProfessional(@NotNull User professional);
}

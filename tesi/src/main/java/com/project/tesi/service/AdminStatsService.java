package com.project.tesi.service;

import com.project.tesi.model.Plan;
import com.project.tesi.model.Slot;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface AdminStatsService {
    List<Plan> getAllPlans();
    List<Slot> getAllBookedSlots();
}

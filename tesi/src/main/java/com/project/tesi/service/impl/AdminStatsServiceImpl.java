package com.project.tesi.service.impl;

import com.project.tesi.model.Plan;
import com.project.tesi.model.Slot;
import com.project.tesi.repository.PlanRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.service.AdminStatsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminStatsServiceImpl implements AdminStatsService {

    private final SlotRepository slotRepository;
    private final PlanRepository planRepository;

    public AdminStatsServiceImpl(SlotRepository slotRepository,
                                  PlanRepository planRepository) {
        this.slotRepository = slotRepository;
        this.planRepository = planRepository;
    }

    @Override
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Override
    public List<Slot> getAllBookedSlots() {
        return slotRepository.findAllBooked();
    }
}

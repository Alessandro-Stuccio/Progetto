package com.project.tesi.service.impl;

import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.Plan;
import com.project.tesi.repository.PlanRepository;
import com.project.tesi.service.PlanService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    public PlanServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Override
    public Plan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Piano", id));
    }

    @Override
    public Plan createPlan(Plan plan) {
        return planRepository.save(plan);
    }

    @Override
    public void deletePlan(Long id) {
        if (!planRepository.existsById(id)) {
            throw new ResourceNotFoundException("Piano", id);
        }
        planRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return planRepository.findByName(name).isPresent();
    }
}

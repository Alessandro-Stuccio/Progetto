package com.project.tesi.service.impl;

import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.Plan;
import com.project.tesi.repository.PlanRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.service.PlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    public PlanServiceImpl(PlanRepository planRepository, SubscriptionRepository subscriptionRepository) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
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
    @Transactional
    public Plan createPlan(Plan plan) {
        return planRepository.save(plan);
    }

    @Override
    @Transactional
    public Plan updatePlan(Long id, Plan updated) {
        Plan existing = getPlanById(id);
        existing.setName(updated.getName());
        existing.setDuration(updated.getDuration());
        existing.setFullPrice(updated.getFullPrice());
        existing.setMonthlyInstallmentPrice(updated.getMonthlyInstallmentPrice());
        existing.setMonthlyCreditsPT(updated.getMonthlyCreditsPT());
        existing.setMonthlyCreditsNutri(updated.getMonthlyCreditsNutri());
        existing.setInsuranceCoverageDetails(updated.getInsuranceCoverageDetails());
        return planRepository.save(existing);
    }

    @Override
    @Transactional
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

    @Override
    public boolean hasSubscribers(Long planId) {
        return subscriptionRepository.existsByPlanId(planId);
    }

    @Override
    @Transactional
    public Plan save(Plan plan) {
        return planRepository.save(plan);
    }
}
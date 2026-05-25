package com.project.tesi.service.impl;

import com.project.tesi.model.Plan;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.PlanRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.AdminStatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminStatsServiceImpl implements AdminStatsService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SlotRepository slotRepository;
    private final PlanRepository planRepository;

    public AdminStatsServiceImpl(UserRepository userRepository,
                                  SubscriptionRepository subscriptionRepository,
                                  SlotRepository slotRepository,
                                  PlanRepository planRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.slotRepository = slotRepository;
        this.planRepository = planRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Slot> getAllBookedSlots() {
        return slotRepository.findAllBooked();
    }
}

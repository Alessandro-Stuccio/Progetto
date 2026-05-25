package com.project.tesi.service;

import com.project.tesi.model.Plan;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface AdminStatsService {
    List<User> getAllUsers();
    List<Subscription> getAllSubscriptions();
    List<Plan> getAllPlans();
    List<Slot> getAllBookedSlots();
}

package com.project.tesi.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;

import java.util.List;

@Validated
public interface AdminService {

    List<User> getAllUsers();

    User findUserById(@NotNull @Min(1) Long id);

    boolean existsUserByEmail(@NotNull String email);

    boolean existsUserByEmailExcluding(@NotNull String email, @NotNull Long excludeId);

    User saveUser(@NotNull User user);

    void deleteUser(@NotNull @Min(1) Long id);

    List<Subscription> getAllSubscriptions();

    Subscription updateSubscriptionCredits(@NotNull @Min(1) Long subscriptionId,
                                           @Min(0) int creditsPT, @Min(0) int creditsNutri);
}

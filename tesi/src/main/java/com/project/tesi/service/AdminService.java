package com.project.tesi.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;

import java.util.List;

@Validated
public interface AdminService {

    List<User> getAllUsers();

    List<User> getModeratorManageableUsers();

    List<User> getModeratorChatContacts();

    User createUser(@NotNull @Valid UserCreateRequestDTO request);

    User createUserAsModerator(@NotNull @Valid UserCreateRequestDTO request);

    User updateUserAsModerator(@NotNull @Min(1) Long id, @NotNull @Valid ModeratorUserUpdateRequest request);

    void deleteUser(@NotNull @Min(1) Long id);

    void deleteUserAsModerator(@NotNull @Min(1) Long id);

    List<Subscription> getAllSubscriptions();

    Subscription updateSubscriptionCredits(@NotNull @Min(1) Long subscriptionId,
                                           @Min(0) int creditsPT, @Min(0) int creditsNutri);

    Plan createPlan(@NotNull @Valid PlanCreateRequestDTO request);

    Plan updatePlan(@NotNull @Min(1) Long id, @NotNull @Valid PlanCreateRequestDTO request);

    void deletePlan(@NotNull @Min(1) Long id);

    User updateUser(@NotNull @Min(1) Long id, @NotNull @Valid ModeratorUserUpdateRequest request);
}

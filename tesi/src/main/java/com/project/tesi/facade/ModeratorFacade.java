package com.project.tesi.facade;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.model.User;

import java.util.List;

public interface ModeratorFacade {
    List<UserResponse> getManageableUsers(User user);
    List<SubscriptionResponse> getAllSubscriptions();
    List<UserResponse> getChatContacts();
    UserResponse createUser(UserCreateRequestDTO request,User user);
    UserResponse updateUser(Long id, ModeratorUserUpdateRequest request,User user);
    void deleteUser(Long id, User user);
    SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri);
}

package com.project.tesi.facade;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;

import java.util.List;

public interface ModeratorFacade {
    List<UserResponse> getManageableUsers();
    List<SubscriptionResponse> getAllSubscriptions();
    List<UserResponse> getChatContacts();
    UserResponse createUser(UserCreateRequestDTO request);
    UserResponse updateUser(Long id, ModeratorUserUpdateRequest request);
    void deleteUser(Long id);
    SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri);
    void closeChat(Long chatId, Long moderatorId);
}

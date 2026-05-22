package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.facade.ModeratorFacade;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.service.AdminService;
import com.project.tesi.service.ChatService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModeratorFacadeImpl implements ModeratorFacade {

    private final AdminService adminService;
    private final ChatService chatService;
    private final UserMapper userMapper;
    private final SubscriptionMapper subscriptionMapper;

    public ModeratorFacadeImpl(AdminService adminService, ChatService chatService,
                               UserMapper userMapper, SubscriptionMapper subscriptionMapper) {
        this.adminService = adminService;
        this.chatService = chatService;
        this.userMapper = userMapper;
        this.subscriptionMapper = subscriptionMapper;
    }

    @Override
    public List<UserResponse> getManageableUsers() {
        return adminService.getModeratorManageableUsers().stream()
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    public List<SubscriptionResponse> getAllSubscriptions() {
        return adminService.getAllSubscriptions().stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getChatContacts() {
        return adminService.getModeratorChatContacts().stream()
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    public UserResponse createUser(UserCreateRequestDTO request) {
        return userMapper.toAdminResponse(adminService.createUserAsModerator(request));
    }

    @Override
    public UserResponse updateUser(Long id, ModeratorUserUpdateRequest request) {
        return userMapper.toAdminResponse(adminService.updateUserAsModerator(id, request));
    }

    @Override
    public void deleteUser(Long id) {
        adminService.deleteUserAsModerator(id);
    }

    @Override
    public SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri) {
        return subscriptionMapper.toResponse(adminService.updateSubscriptionCredits(id, pt, nutri));
    }

    @Override
    public void closeChat(Long chatId, Long moderatorId) {
        chatService.closeChat(chatId, moderatorId);
    }
}

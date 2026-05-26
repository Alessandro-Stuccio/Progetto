package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.facade.ModeratorFacade;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.User;
import com.project.tesi.service.AdminService;
import com.project.tesi.service.ChatService;
import com.project.tesi.service.SubscriptionService;
import com.project.tesi.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class ModeratorFacadeImpl extends AbstractUserManagementFacade implements ModeratorFacade {

    private static final Set<Role> MODERATOR_MANAGEABLE_ROLES =
            EnumSet.of(Role.CLIENT, Role.PERSONAL_TRAINER, Role.NUTRITIONIST);

    private final ChatService chatService;
    private final SubscriptionMapper subscriptionMapper;

    public ModeratorFacadeImpl(AdminService adminService,
                               ChatService chatService,
                               UserService userService,
                               SubscriptionService subscriptionService,
                               UserMapper userMapper,
                               SubscriptionMapper subscriptionMapper) {
        super(adminService, userService, subscriptionService, userMapper);
        this.chatService = chatService;
        this.subscriptionMapper = subscriptionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getManageableUsers() {
        return adminService.getAllUsers().stream()
                .filter(u -> MODERATOR_MANAGEABLE_ROLES.contains(u.getRole()))
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getChatContacts() {
        return adminService.getAllUsers().stream()
                .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.INSURANCE_MANAGER)
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptions() {
        return adminService.getAllSubscriptions().stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequestDTO request) {
        getAuthenticatedModerator();
        Role targetRole = parseRole(request.role());
        if (!MODERATOR_MANAGEABLE_ROLES.contains(targetRole)) {
            throw new UnauthorizedAccessException(
                    "Il moderatore non può creare utenti con ruolo " + targetRole + ".");
        }
        return buildAndSaveUser(request, targetRole);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, ModeratorUserUpdateRequest request) {
        getAuthenticatedModerator();

        User target = userService.getUserById(id);
        if (!MODERATOR_MANAGEABLE_ROLES.contains(target.getRole())) {
            throw new UnauthorizedAccessException(
                    "Il moderatore non può modificare utenti con ruolo " + target.getRole() + ".");
        }

        if (request.role() != null && !request.role().isBlank()) {
            Role newRole = parseRole(request.role());
            if (!MODERATOR_MANAGEABLE_ROLES.contains(newRole)) {
                throw new UnauthorizedAccessException(
                        "Il moderatore può assegnare solo i ruoli: " + MODERATOR_MANAGEABLE_ROLES);
            }
        }

        String email = request.email();
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(target.getEmail())) {
            if (adminService.existsUserByEmailExcluding(email, id)) {
                throw new ResourceAlreadyExistsException("Utente", "email", email);
            }
            target.setEmail(email);
        }

        applyUserUpdates(target, request);

        return userMapper.toAdminResponse(adminService.saveUser(target));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        getAuthenticatedModerator();

        User target = userService.getUserById(id);
        if (!MODERATOR_MANAGEABLE_ROLES.contains(target.getRole())) {
            throw new UnauthorizedAccessException(
                    "Il moderatore non può eliminare utenti con ruolo " + target.getRole() + ".");
        }

        adminService.deleteUser(id);
    }

    @Override
    @Transactional
    public SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri) {
        return subscriptionMapper.toResponse(adminService.updateSubscriptionCredits(id, pt, nutri));
    }

    @Override
    @Transactional
    public void closeChat(Long chatId, Long moderatorId) {
        chatService.closeChat(chatId, moderatorId);
    }

    private User getAuthenticatedModerator() {
        User actor = getAuthenticatedUser();
        if (actor.getRole() != Role.MODERATOR) {
            throw new UnauthorizedAccessException("Solo i moderatori possono eseguire questa operazione.");
        }
        return actor;
    }
}

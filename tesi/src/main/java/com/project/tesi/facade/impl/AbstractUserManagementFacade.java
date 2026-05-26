package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.User;
import com.project.tesi.service.AdminService;
import com.project.tesi.service.SubscriptionService;
import com.project.tesi.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractUserManagementFacade {

    protected final AdminService adminService;
    protected final UserService userService;
    protected final SubscriptionService subscriptionService;
    protected final UserMapper userMapper;

    protected AbstractUserManagementFacade(AdminService adminService,
                                           UserService userService,
                                           SubscriptionService subscriptionService,
                                           UserMapper userMapper) {
        this.adminService = adminService;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.userMapper = userMapper;
    }

    protected UserResponse buildAndSaveUser(UserCreateRequestDTO request, Role targetRole) {
        String email = request.email();
        String firstName = request.firstName();
        String lastName = request.lastName();
        String password = request.password();
        if (email == null || firstName == null || lastName == null || password == null) {
            throw new IllegalArgumentException(
                    "Campi obbligatori mancanti (email, firstName, lastName, password, role).");
        }

        if (adminService.existsUserByEmail(email)) {
            throw new ResourceAlreadyExistsException("Utente", "email", email);
        }

        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(userService.encodePassword(password))
                .role(targetRole)
                .build();

        if (targetRole == Role.CLIENT) {
            if (request.assignedPTId() != null) {
                User assignedPT = adminService.findUserById(request.assignedPTId());
                if (assignedPT.getRole() != Role.PERSONAL_TRAINER) {
                    throw new UnauthorizedAccessException("L'utente assegnato come PT non è un PERSONAL_TRAINER");
                }
                user.setAssignedPT(assignedPT);
            }
            if (request.assignedNutritionistId() != null) {
                User assignedNutri = adminService.findUserById(request.assignedNutritionistId());
                if (assignedNutri.getRole() != Role.NUTRITIONIST) {
                    throw new UnauthorizedAccessException("L'utente assegnato come nutrizionista non è un NUTRITIONIST");
                }
                user.setAssignedNutritionist(assignedNutri);
            }
        }

        User savedUser = adminService.saveUser(user);

        if (targetRole == Role.CLIENT && request.planId() != null && request.paymentFrequency() != null) {
            PaymentFrequency freq;
            try {
                freq = PaymentFrequency.valueOf(request.paymentFrequency());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Frequenza di pagamento non valida: " + request.paymentFrequency());
            }
            subscriptionService.activateSubscription(new PlanRequest(request.planId(), freq), savedUser.getId());
        }

        return userMapper.toAdminResponse(savedUser);
    }

    protected User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedAccessException("Autenticazione richiesta.");
        }
        return userService.getUserByEmail(auth.getName());
    }

    protected void applyUserUpdates(User target, ModeratorUserUpdateRequest request) {
        String firstName = request.firstName();
        if (firstName != null && !firstName.isBlank()) target.setFirstName(firstName);

        String lastName = request.lastName();
        if (lastName != null && !lastName.isBlank()) target.setLastName(lastName);

        String password = request.password();
        if (password != null && !password.isBlank()) target.setPassword(userService.encodePassword(password));

        String roleRaw = request.role();
        if (roleRaw != null && !roleRaw.isBlank()) target.setRole(parseRole(roleRaw));
    }

    protected Role parseRole(String raw) {
        try {
            return Role.valueOf(raw);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Ruolo non valido: " + raw);
        }
    }
}

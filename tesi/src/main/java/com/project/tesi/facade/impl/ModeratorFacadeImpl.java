package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.facade.ChatFacade;
import com.project.tesi.facade.ModeratorFacade;
import com.project.tesi.facade.SubscriptionFacade;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.User;
import com.project.tesi.service.AdminService;
import com.project.tesi.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class ModeratorFacadeImpl implements ModeratorFacade {

    private static final Set<Role> MODERATOR_MANAGEABLE_ROLES =
            EnumSet.of(Role.CLIENT, Role.PERSONAL_TRAINER, Role.NUTRITIONIST);

    private final AdminService adminService;
    private final ChatFacade chatFacade;
    private final UserService userService;
    private final SubscriptionFacade subscriptionFacade;
    private final UserMapper userMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final PasswordEncoder passwordEncoder;

    public ModeratorFacadeImpl(AdminService adminService,
                               ChatFacade chatFacade,
                               UserService userService,
                               SubscriptionFacade subscriptionFacade,
                               UserMapper userMapper,
                               SubscriptionMapper subscriptionMapper,
                               PasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.chatFacade = chatFacade;
        this.userService = userService;
        this.subscriptionFacade = subscriptionFacade;
        this.userMapper = userMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserResponse> getManageableUsers() {
        return adminService.getAllUsers().stream()
                .filter(u -> MODERATOR_MANAGEABLE_ROLES.contains(u.getRole()))
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getChatContacts() {
        return adminService.getAllUsers().stream()
                .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.INSURANCE_MANAGER)
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
    public UserResponse createUser(UserCreateRequestDTO request) {
        getAuthenticatedModerator();

        Role targetRole = parseRole(request.role());
        if (!MODERATOR_MANAGEABLE_ROLES.contains(targetRole)) {
            throw new UnauthorizedAccessException(
                    "Il moderatore non può creare utenti con ruolo " + targetRole + ".");
        }

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
                .password(passwordEncoder.encode(password))
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
            subscriptionFacade.activateSubscription(new PlanRequest(request.planId(), freq), savedUser.getId());
        }

        return userMapper.toAdminResponse(savedUser);
    }

    @Override
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

        String firstName = request.firstName();
        if (firstName != null && !firstName.isBlank()) {
            target.setFirstName(firstName);
        }

        String lastName = request.lastName();
        if (lastName != null && !lastName.isBlank()) {
            target.setLastName(lastName);
        }

        String password = request.password();
        if (password != null && !password.isBlank()) {
            target.setPassword(passwordEncoder.encode(password));
        }

        String roleRaw = request.role();
        if (roleRaw != null && !roleRaw.isBlank()) {
            target.setRole(parseRole(roleRaw));
        }

        return userMapper.toAdminResponse(adminService.saveUser(target));
    }

    @Override
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
    public SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri) {
        return subscriptionMapper.toResponse(adminService.updateSubscriptionCredits(id, pt, nutri));
    }

    @Override
    public void closeChat(Long chatId, Long moderatorId) {
        chatFacade.closeChat(chatId, moderatorId);
    }

    private User getAuthenticatedModerator() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedAccessException("Autenticazione richiesta.");
        }
        User actor = userService.getUserByEmail(auth.getName());
        if (actor.getRole() != Role.MODERATOR) {
            throw new UnauthorizedAccessException("Solo i moderatori possono eseguire questa operazione.");
        }
        return actor;
    }

    private Role parseRole(String raw) {
        try {
            return Role.valueOf(raw);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Ruolo non valido: " + raw);
        }
    }
}

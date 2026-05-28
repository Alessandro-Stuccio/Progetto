package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.LoginRequest;
import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.booking.ProfessionalSoldOutException;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.facade.AuthFacade;
import com.project.tesi.util.BusinessConstants;
import com.project.tesi.facade.SubscriptionFacade;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.model.User;
import com.project.tesi.security.JwtUtil;
import com.project.tesi.dto.response.AuthResult;
import com.project.tesi.service.EmailService;
import com.project.tesi.service.PlanService;
import com.project.tesi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthFacadeImpl implements AuthFacade {

    private static final Logger log = LoggerFactory.getLogger(AuthFacadeImpl.class);
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PlanService planService;
    private final SubscriptionFacade subscriptionFacade;

    public AuthFacadeImpl(JwtUtil jwtUtil,
                          UserService userService,
                          EmailService emailService,
                          PasswordEncoder passwordEncoder,
                          UserMapper userMapper,
                          PlanService planService,
                          SubscriptionFacade subscriptionFacade) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.planService = planService;
        this.subscriptionFacade = subscriptionFacade;
    }

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("Utente", "email", request.email());
        }

        User newUser = userMapper.toUser(request);
        newUser.setPassword(userService.encodePassword(request.password()));

        assignProfessional(newUser, request.selectedPtId(), Role.PERSONAL_TRAINER);
        assignProfessional(newUser, request.selectedNutritionistId(), Role.NUTRITIONIST);

        User savedUser = userService.save(newUser);

        if (request.selectedPlanId() != null && request.paymentFrequency() != null) {
            Plan plan = planService.getPlanById(request.selectedPlanId());
            subscriptionFacade.activateSubscription(savedUser, plan, request.paymentFrequency());
        }

        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());
        } catch (Exception e) {
            log.warn("Impossibile inviare email di benvenuto a {}: {}", savedUser.getEmail(), e.getMessage());
        }

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult login(LoginRequest request) {
        User user = userService.getUserByEmail(request.email());
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenziali non valide");
        }
        String jwtToken = jwtUtil.generateToken(user);
        return AuthResult.builder().token(jwtToken).user(user).build();
    }

    @Override
    @Transactional(readOnly = true)
    public void forgotPassword(String email) {
        User user = userService.getUserByEmail(email);
        String resetToken = jwtUtil.generatePasswordResetToken(user.getEmail());
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetToken);
        } catch (Exception e) {
            log.error("Errore nell'invio dell'email di reset password a {}: {}", email, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        String email = jwtUtil.validatePasswordResetToken(token);
        User user = userService.getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
        try {
            emailService.sendPasswordChangeEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            log.warn("Impossibile inviare email di avvenuto reset password a {}: {}", user.getEmail(), e.getMessage());
        }
        log.info("Password reimpostata con successo per l'utente {}", user.getEmail());
    }

    private void assignProfessional(User user, Long proId, Role expectedRole) {
        if (proId == null) {
            return;
        }
        User professional = userService.getUserById(proId);
        if (professional.getRole() != expectedRole) {
            throw new IllegalArgumentException("L'ID fornito non corrisponde a un " + expectedRole + ".");
        }
        long activeClients = expectedRole == Role.PERSONAL_TRAINER
                ? userService.countByAssignedPT(professional)
                : userService.countByAssignedNutritionist(professional);
        if (activeClients >= BusinessConstants.MAX_CLIENTS_PER_PROFESSIONAL) {
            throw new ProfessionalSoldOutException(professional.getFirstName());
        }
        if (expectedRole == Role.PERSONAL_TRAINER) {
            user.setAssignedPT(professional);
        } else {
            user.setAssignedNutritionist(professional);
        }
    }
}

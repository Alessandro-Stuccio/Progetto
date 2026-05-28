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

/**
 * Implementazione di {@link AuthFacade}.
 * Gestisce registrazione, login e reset password coordinando
 * {@code UserService}, {@code EmailService} e {@code JwtUtil}.
 */
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

    /**
     * Crea un nuovo utente con ruolo {@code CLIENT}, codifica la password,
     * assegna opzionalmente personal trainer e nutrizionista verificandone
     * la capienza massima, attiva l'abbonamento se piano e frequenza sono forniti,
     * e invia un'email di benvenuto (errori SMTP non bloccanti).
     *
     * @param request dati di registrazione (email, password, professionisti, piano)
     * @return {@link UserResponse} con i dati dell'utente appena creato
     * @throws ResourceAlreadyExistsException se l'email è già in uso
     * @throws ProfessionalSoldOutException   se il professionista selezionato ha raggiunto la capienza massima
     */
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

    /**
     * Autentica l'utente confrontando la password con {@code PasswordEncoder},
     * genera un JWT tramite {@code JwtUtil} e restituisce token e dati utente.
     *
     * @param request credenziali di accesso (email e password)
     * @return {@link AuthResult} con token JWT e riferimento all'entità {@link User}
     * @throws BadCredentialsException se la password non corrisponde
     */
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

    /**
     * Genera un token JWT per il reset password (scadenza 30 min) tramite {@code JwtUtil}
     * e lo invia via email all'utente. Errori SMTP vengono loggati ma non propagati.
     *
     * @param email indirizzo email dell'utente che ha richiesto il reset
     */
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

    /**
     * Valida il token di reset (verifica firma e scadenza 30 min tramite {@code JwtUtil}),
     * aggiorna la password dell'utente con la nuova password codificata
     * e invia un'email di conferma avvenuta modifica.
     *
     * @param token       token JWT di reset password ricevuto via email
     * @param newPassword nuova password in chiaro da codificare e salvare
     */
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

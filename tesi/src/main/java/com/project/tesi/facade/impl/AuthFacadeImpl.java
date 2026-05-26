package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.LoginRequest;
import com.project.tesi.facade.AuthFacade;
import com.project.tesi.model.User;
import com.project.tesi.security.CustomUserDetailsService;
import com.project.tesi.security.JwtUtil;
import com.project.tesi.service.AuthResult;
import com.project.tesi.service.AuthService;
import com.project.tesi.service.EmailService;
import com.project.tesi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthFacadeImpl implements AuthFacade {

    private static final Logger log = LoggerFactory.getLogger(AuthFacadeImpl.class);

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;

    public AuthFacadeImpl(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          AuthService authService,
                          UserService userService,
                          EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String jwtToken = jwtUtil.generateToken(userDetails);
        User user = userService.getUserByEmail(request.email());
        return new AuthResult(jwtToken, user);
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
        User user = authService.resetPassword(email, newPassword);
        try {
            emailService.sendPasswordChangeEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            log.warn("Impossibile inviare email di avvenuto reset password a {}: {}", user.getEmail(), e.getMessage());
        }
        log.info("Password reimpostata con successo per l'utente {}", user.getEmail());
    }
}

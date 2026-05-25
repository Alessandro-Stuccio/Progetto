package com.project.tesi.service.impl;

import com.project.tesi.dto.request.LoginRequest;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.service.AuthResult;
import com.project.tesi.model.User;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.security.CustomUserDetailsService;
import com.project.tesi.security.JwtUtil;
import com.project.tesi.service.AuthService;
import com.project.tesi.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           CustomUserDetailsService userDetailsService,
                           JwtUtil jwtUtil,
                           UserRepository userRepository,
                           EmailService emailService,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResult login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String jwtToken = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Utente", "email", request.email()));

        return new AuthResult(jwtToken, user);
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", "email", email));

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

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", "email", email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        try {
            emailService.sendPasswordChangeEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            log.warn("Impossibile inviare email di avvenuto reset password a {}: {}", user.getEmail(), e.getMessage());
        }

        log.info("Password reimpostata con successo per l'utente {}", user.getEmail());
    }
}

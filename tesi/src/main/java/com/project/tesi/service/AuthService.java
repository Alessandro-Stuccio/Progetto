package com.project.tesi.service;

import org.springframework.validation.annotation.Validated;

import com.project.tesi.dto.request.LoginRequest;

@Validated
public interface AuthService {

    /** Autentica un utente e restituisce il token JWT con i dati del profilo. */
    AuthResult login(LoginRequest request);

    /** Genera un token di reset password e invia l'email con il link. */
    void forgotPassword(String email);

    /** Reimposta la password dell'utente usando il token ricevuto via email. */
    void resetPassword(String token, String newPassword);
}

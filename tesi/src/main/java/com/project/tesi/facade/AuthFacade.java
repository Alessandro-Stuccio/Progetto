package com.project.tesi.facade;

import com.project.tesi.dto.request.LoginRequest;
import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.AuthResult;

/**
 * Autenticazione e gestione delle credenziali.
 */
public interface AuthFacade {

    /**
     * Registra un nuovo utente.
     */
    UserResponse registerUser(RegisterRequest request);

    /**
     * Verifica le credenziali e restituisce il token JWT.
     */
    AuthResult login(LoginRequest request);

    /**
     * Avvia il recupero password inviando l'email con il link di reset.
     */
    void forgotPassword(String email);

    /**
     * Imposta la nuova password verificando il token di reset ricevuto via email.
     */
    void resetPassword(String token, String newPassword);
}

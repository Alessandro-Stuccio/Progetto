package com.project.tesi.facade;

import com.project.tesi.dto.request.LoginRequest;
import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.AuthResult;

/**
 * Facade per autenticazione e gestione delle credenziali.
 */
public interface AuthFacade {

    /**
     * Registra un nuovo utente nel sistema.
     *
     * @param request dati di registrazione dell'utente
     * @return informazioni dell'utente registrato
     */
    UserResponse registerUser(RegisterRequest request);

    /**
     * Autentica un utente e restituisce il token JWT.
     *
     * @param request credenziali di accesso (email e password)
     * @return risultato dell'autenticazione con token JWT
     */
    AuthResult login(LoginRequest request);

    /**
     * Invia un'email di reset password all'indirizzo specificato.
     *
     * @param email indirizzo email dell'utente che ha richiesto il reset
     */
    void forgotPassword(String email);

    /**
     * Applica la nuova password utilizzando il token di reset ricevuto via email.
     *
     * @param token       token di reset valido ricevuto via email
     * @param newPassword nuova password da impostare
     */
    void resetPassword(String token, String newPassword);
}

package com.project.kore.facade;

import com.project.kore.dto.request.LoginRequest;
import com.project.kore.dto.request.RegisterRequest;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.dto.response.AuthResult;
import com.project.kore.exception.booking.ProfessionalSoldOutException;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Autenticazione e gestione delle credenziali.
 */
public interface AuthFacade {

    /**
     * Registra un nuovo utente.
     *
     * @param request dati di registrazione (anagrafica, credenziali, eventuale professionista assegnato)
     * @return i dati dell'utente appena registrato
     * @throws ResourceAlreadyExistsException se l'email è già in uso
     * @throws IllegalArgumentException       se un id di professionista assegnato non corrisponde al ruolo atteso
     * @throws ProfessionalSoldOutException   se il professionista scelto ha già raggiunto il massimo dei clienti
     */
    UserResponse registerUser(RegisterRequest request);

    /**
     * Verifica le credenziali e restituisce il token JWT.
     *
     * @param request email e password
     * @return l'esito con il token JWT e i dati utente
     * @throws BadCredentialsException se le credenziali non sono valide
     */
    AuthResult login(LoginRequest request);

    /**
     * Avvia il recupero password inviando l'email con il link di reset.
     *
     * @param email email dell'account per cui avviare il reset
     */
    void forgotPassword(String email);

    /**
     * Imposta la nuova password verificando il token di reset ricevuto via email.
     *
     * @param token       token di reset ricevuto via email
     * @param newPassword nuova password in chiaro
     */
    void resetPassword(String token, String newPassword);
}

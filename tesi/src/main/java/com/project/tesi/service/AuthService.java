package com.project.tesi.service;

import com.project.tesi.model.User;
import org.springframework.validation.annotation.Validated;

@Validated
public interface AuthService {

    /** Aggiorna la password dell'utente identificato da email e restituisce l'utente aggiornato. */
    User resetPassword(String email, String newPassword);
}

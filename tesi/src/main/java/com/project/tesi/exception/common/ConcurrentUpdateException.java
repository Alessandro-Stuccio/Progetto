package com.project.tesi.exception.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Eccezione lanciata quando si verifica un conflitto di aggiornamento concorrente
 * ({@link org.springframework.orm.ObjectOptimisticLockingFailureException} catturato).
 * Mappa su HTTP 409 CONFLICT.
 */
public class ConcurrentUpdateException extends ResponseStatusException {
    public ConcurrentUpdateException(String message) {
        super(HttpStatus.CONFLICT,message);
    }
}

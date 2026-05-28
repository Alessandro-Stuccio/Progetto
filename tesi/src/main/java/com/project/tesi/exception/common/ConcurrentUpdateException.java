package com.project.tesi.exception.common;

import org.springframework.http.HttpStatus;

/**
 * Eccezione lanciata quando si verifica un conflitto di aggiornamento concorrente
 * ({@link org.springframework.orm.ObjectOptimisticLockingFailureException} catturato).
 * Mappa su HTTP 409 CONFLICT.
 */
public class ConcurrentUpdateException extends BaseException {
    public ConcurrentUpdateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

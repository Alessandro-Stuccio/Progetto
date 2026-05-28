package com.project.tesi.exception.booking;

import com.project.tesi.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Eccezione lanciata quando l'utente tenta di prenotare senza un abbonamento attivo
 * o con crediti esauriti. Mappa su HTTP 400.
 */
public class SubscriptionExpiredException extends BaseException {
    public SubscriptionExpiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

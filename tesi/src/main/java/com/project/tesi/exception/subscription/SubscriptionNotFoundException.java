package com.project.tesi.exception.subscription;

import com.project.tesi.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Eccezione lanciata quando non viene trovato un abbonamento per l'utente (404 Not Found).
 * che è specifica per il flusso di prenotazione (422).
 */
public class SubscriptionNotFoundException extends BaseException {

    /** Costruttore con messaggio predefinito. */
    public SubscriptionNotFoundException() {
        super("Nessun abbonamento attivo trovato.", HttpStatus.NOT_FOUND);
    }

    /** @param message messaggio personalizzato */
    public SubscriptionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}


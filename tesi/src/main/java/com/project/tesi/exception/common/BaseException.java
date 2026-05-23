package com.project.tesi.exception.common;

import org.springframework.http.HttpStatus;

/**
 * Classe astratta base per tutte le eccezioni personalizzate dell'applicazione.
 * Ogni sottoclasse definisce il proprio codice HTTP tramite {@link HttpStatus},
 * che viene poi usato dal {@link com.project.tesi.exception.GlobalExceptionHandler}
 * per costruire la risposta di errore.
 */
public abstract class BaseException extends RuntimeException {

    /** Codice di stato HTTP associato all'eccezione (es. 404, 409, 422). */
    private final HttpStatus status;

    /**
     * @param message messaggio di errore leggibile
     * @param status  codice HTTP da restituire al client
     */
    protected BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * @param message messaggio di errore leggibile
     * @param status  codice HTTP da restituire al client
     * @param cause   eccezione originale (per il logging)
     */
    protected BaseException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}


package com.project.tesi.exception.common;

import org.springframework.http.HttpStatus;

/**
 * Eccezione per risorse non trovate (404 Not Found).
 * Usata quando un'entità richiesta tramite ID o altro campo non esiste nel database.
 */
public class CustomResourceNotFoundException extends BaseException {

    /** @param message messaggio personalizzato */
    public CustomResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    /**
     * Costruttore con nome risorsa e ID per messaggi auto-generati.
     * @param resourceName tipo di risorsa (es. "Utente", "Piano")
     * @param id           ID non trovato
     */
    public CustomResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " con ID " + id + " non trovato.", HttpStatus.NOT_FOUND);
    }

    /**
     * Costruttore con nome risorsa, campo e valore per messaggi auto-generati.
     * @param resourceName tipo di risorsa
     * @param fieldName    nome del campo (es. "email")
     * @param fieldValue   valore cercato
     */
    public CustomResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(resourceName + " con " + fieldName + " '" + fieldValue + "' non trovato.", HttpStatus.NOT_FOUND);
    }
}


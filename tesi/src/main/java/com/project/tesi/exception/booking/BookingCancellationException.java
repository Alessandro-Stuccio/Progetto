package com.project.tesi.exception.booking;

import com.project.tesi.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Eccezione lanciata quando la cancellazione di una prenotazione non è consentita
 * (es. stato errato o meno di 24 ore all'appuntamento). Mappa su HTTP 400.
 */
public class BookingCancellationException extends BaseException {
    public BookingCancellationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

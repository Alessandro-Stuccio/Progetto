package com.project.tesi.service;

import org.springframework.validation.annotation.Validated;

import com.project.tesi.model.User;
import com.project.tesi.model.Slot;

/**
 * Servizio per la generazione di link per videoconferenze.
 */
@Validated
public interface VideoConferenceService {

    /**
     * Genera un link Jitsi per la videoconferenza relativa allo slot prenotato
     * tra un utente e un professionista.
     *
     * @param user         utente cliente che ha prenotato lo slot
     * @param professional utente professionista
     * @param slot         slot di prenotazione per cui generare il link
     * @return URL della stanza Jitsi da usare per il meeting
     */
    String generateMeetingLink(User user, User professional, Slot slot);
}


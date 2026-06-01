package com.project.tesi.service;

import org.springframework.validation.annotation.Validated;

import com.project.tesi.model.User;
import com.project.tesi.model.Slot;

/** Genera i link per le videoconferenze delle prenotazioni. */
@Validated
public interface VideoConferenceService {

    /** Crea la stanza Jitsi per l'incontro tra cliente e professionista su quello slot e ne ritorna l'URL. */
    String generateMeetingLink(User user, User professional, Slot slot);
}


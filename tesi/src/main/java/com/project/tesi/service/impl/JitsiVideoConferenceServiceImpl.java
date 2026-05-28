package com.project.tesi.service.impl;

import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.service.VideoConferenceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementazione di VideoConferenceService basata su Jitsi Meet. Genera link pubblici
 * della forma https://meet.jit.si/kore-{slotId}-{userId}.
 */
@Service
public class JitsiVideoConferenceServiceImpl implements VideoConferenceService {

    @Value("${jitsi.base-url:https://meet.jit.si/Kore_Consulto_}")
    private String jitsiBaseUrl;

    /**
     * Il room name è composto dal prefisso configurato in 'jitsi.base-url',
     * dall'id del client, dall'id del professionista e da un suffisso UUID a 8 caratteri
     * per unicità. Il link è pubblico e non richiede autenticazione Jitsi.
     */
    @Override
    public String generateMeetingLink(User user, User professional, Slot slot) {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s%d_%d_%s", jitsiBaseUrl, user.getId(), professional.getId(), uniqueId);
    }
}

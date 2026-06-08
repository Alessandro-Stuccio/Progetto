package com.project.kore.config;

import com.project.kore.model.User;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener che tiene traccia della "presence" degli utenti.
 * Usiamo tre ConcurrentHashMap incrociate per mappare userId <-> sessionId <-> roomId.
 * Questo ci permette di sapere in tempo reale chi è dentro una chat e decidere 
 * se mandare una notifica push (se l'utente è offline o in un'altra pagina) o meno.
 */
@Component
public class WebSocketEventListener {

    /** Mappa userId → insieme delle sessioni WebSocket attive dell'utente. */
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    /** Mappa inversa sessionId → userId per il cleanup alla disconnessione. */
    private final Map<String, Long> sessionUser = new ConcurrentHashMap<>();

    /** Mappa sessionId → insieme delle stanze chat in cui la sessione è attualmente presente. */
    private final Map<String, Set<String>> sessionRooms = new ConcurrentHashMap<>();

    // Estrae il Principal JWT al momento della connessione e registra la sessione
    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();
        if (sessionId != null && principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof User user) {
            Long userId = user.getId();
            sessionUser.put(sessionId, userId);
            userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
            sessionRooms.put(sessionId, ConcurrentHashMap.newKeySet());
        }
    }

    // Pulisce in modo sicuro le mappe quando un utente chiude la tab o perde la connessione
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        if (sessionId == null) return;
        Long userId = sessionUser.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) userSessions.remove(userId);
            }
        }
        sessionRooms.remove(sessionId);
    }

    // Registra l'ingresso in una chat room
    public void joinRoom(String sessionId, String roomId) {
        Set<String> rooms = sessionRooms.get(sessionId);
        if (rooms != null) rooms.add(roomId);
    }

    // Registra l'uscita da una chat room
    public void leaveRoom(String sessionId, String roomId) {
        Set<String> rooms = sessionRooms.get(sessionId);
        if (rooms != null) rooms.remove(roomId);
    }

    // Verifica incrociata: l'utente ha almeno una sessione attiva dentro quella specifica stanza?
    public boolean isUserInRoom(Long userId, String roomId) {
        Set<String> sessions = userSessions.get(userId);
        if (sessions == null) return false;
        return sessions.stream().anyMatch(sid -> {
            Set<String> rooms = sessionRooms.get(sid);
            return rooms != null && rooms.contains(roomId);
        });
    }
}

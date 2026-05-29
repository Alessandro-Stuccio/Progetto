package com.project.tesi.controller;

import com.project.tesi.dto.request.SendMessageRequest;
import com.project.tesi.dto.response.ChatMessageResponse;
import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ConversationPreviewResponse;
import com.project.tesi.facade.ChatFacade;
import com.project.tesi.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint REST per la chat. Recupera lo storico dei messaggi e lo stato delle conversazioni,
 * usato al caricamento iniziale prima del WebSocket.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatFacade chatFacade;

    public ChatController(ChatFacade chatFacade) {
        this.chatFacade = chatFacade;
    }

    /** Crea una nuova chat tra l'utente autenticato e il destinatario, o recupera quella esistente. */
    @PostMapping("/create/{receiverId}")
    public ResponseEntity<Long> createChat(@AuthenticationPrincipal User user,
                                            @PathVariable Long receiverId) {
        return ResponseEntity.ok(chatFacade.createChat(user.getId(), receiverId));
    }

    /** Invia un nuovo messaggio da parte dell'utente autenticato. */
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@AuthenticationPrincipal User user,
                                                            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(chatFacade.sendMessage(request, user.getId()));
    }

    /** Recupera la cronologia dei messaggi di una chat (paginata). */
    @GetMapping("/conversation/{chatId}")
    public ResponseEntity<List<ChatMessageResponse>> getConversation(
            @AuthenticationPrincipal User user,
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(chatFacade.getConversation(chatId, user.getId(), page, size));
    }

    /** Recupera la lista di tutte le conversazioni dell'utente autenticato con anteprima ultimo messaggio. */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationPreviewResponse>> getUserConversations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatFacade.getUserConversations(user.getId()));
    }

    /** Segna come letti tutti i messaggi ricevuti in una chat. */
    @PutMapping("/read/{chatId}")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal User user,
                                            @PathVariable Long chatId) {
        chatFacade.markAsRead(chatId, user.getId());
        return ResponseEntity.ok().build();
    }

    /** Restituisce il conteggio totale dei messaggi non letti per l'utente autenticato. */
    @GetMapping("/unread")
    public ResponseEntity<Integer> getTotalUnreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatFacade.getTotalUnreadCount(user.getId()));
    }

    /** Restituisce il moderatore di supporto assegnato all'utente (riusa la conversazione esistente o assegna il meno carico). */
    @GetMapping("/moderator")
    public ResponseEntity<ClientBasicInfoResponse> getModerator(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatFacade.getModerator(user));
    }

    /** Chiude una chat (solo moderatore o admin). L'utente può riaprirla inviando un nuovo messaggio. */
    @PostMapping("/{chatId}/close")
    public ResponseEntity<Void> closeChat(@PathVariable Long chatId,
                                           @AuthenticationPrincipal User user) {
        chatFacade.closeChat(chatId, user.getId());
        return ResponseEntity.noContent().build();
    }
}

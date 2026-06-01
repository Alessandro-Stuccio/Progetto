package com.project.tesi.service;

import com.project.tesi.model.Chat;
import com.project.tesi.model.Message;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** Gestione dei messaggi dentro una chat. */
@Validated
public interface MessageService {

    Message saveMessage(@NotNull Chat chat, @NotNull User sender, @NotBlank String content);

    /** Messaggi della chat in pagine (page 0-based). */
    List<Message> getMessages(@NotNull @Min(1) Long chatId, @Min(0) int page, @Min(1) int size);

    /** Segna come consegnati i messaggi della chat ancora in sospeso per il destinatario. */
    void markAsDelivered(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);

    /** Segna come letti i messaggi non letti della chat per l'utente. */
    void markAsRead(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);

    /** Messaggi non letti dell'utente su tutte le sue chat. */
    int getTotalUnreadCount(@NotNull @Min(1) Long userId);

    Message getLastMessage(@NotNull @Min(1) Long chatId);

    /** Messaggi non letti dell'utente nella singola chat. */
    int getUnreadCount(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);
}

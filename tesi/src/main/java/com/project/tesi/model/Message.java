package com.project.tesi.model;

import com.project.tesi.builder.MessageBuilder;
import com.project.tesi.builder.impl.MessageBuilderImpl;
import com.project.tesi.enums.MessageStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entità JPA per un singolo messaggio di chat.
 *
 * <p>Il ciclo di vita dello stato segue la progressione: {@code SENT} → {@code DELIVERED} → {@code READ}.
 *
 * <p>Relazioni chiave:
 * <ul>
 *   <li>{@code chat} — la conversazione a cui appartiene il messaggio; non nullable.</li>
 * </ul>
 *
 * <p>Il campo {@code sentByUser1} permette di risalire al mittente senza un'ulteriore FK su {@code User},
 * sfruttando la simmetria della relazione già espressa in {@code Chat}.
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Testo del messaggio inviato. */
    private String content;

    /** Data e ora di invio del messaggio. */
    private LocalDateTime timeStamp;

    /**
     * Stato corrente del messaggio nel ciclo di consegna.
     * Valore di default {@code SENT} alla creazione; aggiornato a {@code DELIVERED} e poi {@code READ}
     * dal servizio di messaggistica in tempo reale.
     */
    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT;

    /**
     * {@code true} se il messaggio è stato inviato da {@code user1} della chat associata,
     * {@code false} se inviato da {@code user2}. Evita una FK aggiuntiva verso {@code User}.
     */
    private boolean sentByUser1;

    /** Chat a cui appartiene il messaggio; non nullable. */
    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_chat_id"))
    private Chat chat;

    public Message() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimeStamp() { return timeStamp; }
    public void setTimeStamp(LocalDateTime timeStamp) { this.timeStamp = timeStamp; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public boolean isSentByUser1() { return sentByUser1; }
    public void setSentByUser1(boolean sentByUser1) { this.sentByUser1 = sentByUser1; }

    public Chat getChat() { return chat; }
    public void setChat(Chat chat) { this.chat = chat; }

    public static MessageBuilder builder() {
        return new MessageBuilderImpl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message that = (Message) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Message{id=" + id + ", timeStamp=" + timeStamp + ", sentByUser1=" + sentByUser1 + "}";
    }
}

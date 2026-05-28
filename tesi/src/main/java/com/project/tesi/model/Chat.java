package com.project.tesi.model;

import com.project.tesi.builder.ChatBuilder;
import com.project.tesi.builder.impl.ChatBuilderImpl;
import com.project.tesi.enums.ChatStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entità JPA per una conversazione bidirezionale tra due utenti.
 *
 * <p>Vincoli JPA rilevanti:
 * <ul>
 *   <li>Vincolo unico su {@code (user1_id, user2_id)} — impedisce la creazione di chat duplicate
 *       tra la stessa coppia di utenti.</li>
 *   <li>{@code messages} — relazione {@code @OneToMany} con cascade {@code ALL}; i messaggi
 *       vengono eliminati a cascata se la chat viene rimossa.</li>
 * </ul>
 *
 * <p>Lo stato {@code OPEN}/{@code CLOSED} è gestibile dai moderatori tramite apposita API.
 * I campi {@code closedAt} e {@code closedBy} vengono valorizzati solo alla chiusura.
 */
@Entity
@Table(name = "chats", uniqueConstraints = {
        @UniqueConstraint(name = "uq_chat_users", columnNames = {"user1_id", "user2_id"})
})
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Primo partecipante della conversazione; determina la direzione del campo {@code sentByUser1} nei messaggi. */
    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_user1_id"))
    private User user1;

    /** Secondo partecipante della conversazione. */
    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_user2_id"))
    private User user2;

    /** Lista dei messaggi appartenenti a questa chat; eliminati a cascata con la chat. */
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();

    private LocalDateTime createdAt;

    /** Stato corrente della chat; valore di default {@code OPEN} alla creazione. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatStatus status = ChatStatus.OPEN;

    /** Timestamp in cui la chat è stata chiusa; {@code null} se ancora aperta. */
    private LocalDateTime closedAt;

    /** Moderatore che ha chiuso la chat; {@code null} se la chat è ancora aperta. Caricato lazy. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by_id", foreignKey = @ForeignKey(name = "fk_chat_closed_by_id"))
    private User closedBy;

    public Chat() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser1() { return user1; }
    public void setUser1(User user1) { this.user1 = user1; }

    public User getUser2() { return user2; }
    public void setUser2(User user2) { this.user2 = user2; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public ChatStatus getStatus() { return status; }
    public void setStatus(ChatStatus status) { this.status = status; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public User getClosedBy() { return closedBy; }
    public void setClosedBy(User closedBy) { this.closedBy = closedBy; }

    public static ChatBuilder builder() {
        return new ChatBuilderImpl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat that = (Chat) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Chat{id=" + id + ", createdAt=" + createdAt + ", status=" + status + "}";
    }
}

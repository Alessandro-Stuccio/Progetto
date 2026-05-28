package com.project.tesi.model;

import com.project.tesi.builder.UserBuilder;
import com.project.tesi.builder.impl.UserBuilderImpl;
import com.project.tesi.enums.Role;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Entità JPA che rappresenta un utente della piattaforma.
 * Implementa {@link UserDetails} di Spring Security. L'email funge da username
 * ({@link #getUsername()} restituisce email). Un account disabilitato
 * ({@code deleted=true}) non può autenticarsi ({@link #isEnabled()} = false).
 * Il campo {@code version} supporta l'optimistic locking.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_email", columnNames = {"email"})
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Versione per l'optimistic locking; incrementata automaticamente da JPA ad ogni aggiornamento. */
    @Version
    private Integer version;

    /** Indirizzo email dell'utente; funge da username univoco per l'autenticazione. */
    @Column(nullable = false)
    private String email;

    /** Password cifrata con BCrypt. */
    @Column(nullable = false)
    private String password;

    /** Immagine del profilo come stringa Base64 o URL; può essere {@code null}. */
    @Column(columnDefinition = "TEXT")
    private String profilePicture;

    private String firstName;
    private String lastName;

    /** Ruolo dell'utente che determina permessi e accesso alle funzionalità. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Personal trainer assegnato al cliente; {@code null} per ruoli non CLIENT. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_pt_id", foreignKey = @ForeignKey(name = "fk_user_assigned_pt_id"))
    private User assignedPT;

    /** Nutrizionista assegnato al cliente; {@code null} per ruoli non CLIENT. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_nutritionist_id", foreignKey = @ForeignKey(name = "fk_user_assigned_nutritionist_id"))
    private User assignedNutritionist;

    /** Flag di soft-delete: se {@code true} l'account è disabilitato ma non rimosso dal DB. */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    /** Timestamp di creazione del record; impostato automaticamente e non modificabile. */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp dell'ultimo aggiornamento del record; aggiornato automaticamente da Hibernate. */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public User getAssignedPT() { return assignedPT; }
    public void setAssignedPT(User assignedPT) { this.assignedPT = assignedPT; }

    public User getAssignedNutritionist() { return assignedNutritionist; }
    public void setAssignedNutritionist(User assignedNutritionist) { this.assignedNutritionist = assignedNutritionist; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static UserBuilder builder() {
        return new UserBuilderImpl();
    }

    /**
     * Restituisce i ruoli Spring Security dell'utente nel formato {@code ROLE_<RUOLO>}
     * (es. {@code ROLE_CLIENT}, {@code ROLE_ADMIN}).
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Restituisce il nome completo dell'utente come concatenazione di
     * {@code firstName} e {@code lastName} separati da uno spazio.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Restituisce l'email come username per Spring Security.
     * Non esiste un campo username separato: l'email è l'identificativo univoco.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Restituisce {@code true} se l'account non è stato eliminato con soft-delete.
     * Un account con {@code deleted=true} non può autenticarsi.
     */
    @Override
    public boolean isEnabled() {
        return !deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', firstName='" + firstName + "', lastName='" + lastName + "', role=" + role + "}";
    }
}

package com.project.tesi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entità JPA per la registrazione delle richieste HTTP.
 * Popolata da {@code AuditInterceptor} per ogni chiamata alle API.
 * Traccia utente, metodo, path, IP, status code e body della richiesta.
 *
 * <p>Vincoli JPA rilevanti:
 * <ul>
 *   <li>{@code logged_at} — non nullable, indica il momento esatto della registrazione.</li>
 *   <li>{@code request_body} — troncato a 4000 caratteri per evitare payload eccessivi.</li>
 * </ul>
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Timestamp del momento in cui la richiesta è stata registrata nel log. */
    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    /** Email dell'utente autenticato, oppure la stringa {@code "anonimo"} per richieste non autenticate. */
    @Column(name = "user_identity", length = 255)
    private String userIdentity;

    /** Metodo HTTP della richiesta (es. {@code GET}, {@code POST}, {@code DELETE}). */
    @Column(name = "http_method", length = 10)
    private String httpMethod;

    /** Path dell'endpoint invocato (es. {@code /api/bookings/1}). */
    @Column(name = "http_path", length = 500)
    private String httpPath;

    /** Indirizzo IP del client che ha effettuato la richiesta. */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /** Codice HTTP della risposta restituita al client (es. {@code 200}, {@code 403}, {@code 500}). */
    @Column(name = "http_status")
    private Integer httpStatus;

    /**
     * Corpo della richiesta HTTP, troncato a 4000 caratteri.
     * Può essere {@code null} per richieste senza body (es. GET).
     */
    @Column(name = "request_body", length = 4000)
    private String requestBody;

    public AuditLog() {}

    private AuditLog(Long id, LocalDateTime loggedAt, String userIdentity, String httpMethod,
                     String httpPath, String ipAddress, Integer httpStatus, String requestBody) {
        this.id = id;
        this.loggedAt = loggedAt;
        this.userIdentity = userIdentity;
        this.httpMethod = httpMethod;
        this.httpPath = httpPath;
        this.ipAddress = ipAddress;
        this.httpStatus = httpStatus;
        this.requestBody = requestBody;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }

    public String getUserIdentity() { return userIdentity; }
    public void setUserIdentity(String userIdentity) { this.userIdentity = userIdentity; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getHttpPath() { return httpPath; }
    public void setHttpPath(String httpPath) { this.httpPath = httpPath; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog that = (AuditLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuditLog{id=" + id + ", loggedAt=" + loggedAt + ", userIdentity='" + userIdentity + "', httpMethod='" + httpMethod + "', httpPath='" + httpPath + "', httpStatus=" + httpStatus + "}";
    }

    public static class Builder {
        private Long id;
        private LocalDateTime loggedAt;
        private String userIdentity;
        private String httpMethod;
        private String httpPath;
        private String ipAddress;
        private Integer httpStatus;
        private String requestBody;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder loggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; return this; }
        public Builder userIdentity(String userIdentity) { this.userIdentity = userIdentity; return this; }
        public Builder httpMethod(String httpMethod) { this.httpMethod = httpMethod; return this; }
        public Builder httpPath(String httpPath) { this.httpPath = httpPath; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder httpStatus(Integer httpStatus) { this.httpStatus = httpStatus; return this; }
        public Builder requestBody(String requestBody) { this.requestBody = requestBody; return this; }

        public AuditLog build() {
            return new AuditLog(id, loggedAt, userIdentity, httpMethod, httpPath, ipAddress, httpStatus, requestBody);
        }
    }
}

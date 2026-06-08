package com.project.kore.dto.response;

import com.project.kore.enums.Role;
import java.util.Objects;

/**
 * Risposta al login: il token JWT e i dati essenziali dell'utente autenticato.
 */
public class AuthResponse {

    private String token;
    private String type;
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String profilePicture;

    private AuthResponse() {
        this.type = "Bearer";
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthResponse that = (AuthResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(email, that.email) && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, token);
    }

    @Override
    public String toString() {
        return "AuthResponse{id=" + id + ", email='" + email + "', role=" + role + ", type='" + type + "'}";
    }

    public static class Builder {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private Role role;
        private String profilePicture;

        public Builder token(String token) { this.token = token; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder id(Long id) { this.id = id; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder profilePicture(String profilePicture) { this.profilePicture = profilePicture; return this; }

        public AuthResponse build() {
            AuthResponse response = new AuthResponse();
            response.token = this.token;
            response.type = this.type;
            response.id = this.id;
            response.firstName = this.firstName;
            response.lastName = this.lastName;
            response.email = this.email;
            response.role = this.role;
            response.profilePicture = this.profilePicture;
            return response;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}

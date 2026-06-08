package com.project.kore.dto.response;

import java.util.Objects;

/**
 * Informazioni essenziali di un utente, usate dove non serve il profilo completo.
 */
public class ClientBasicInfoResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureUrl;
    private String role;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientBasicInfoResponse that = (ClientBasicInfoResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "ClientBasicInfoResponse{id=" + id + ", email='" + email + "', role='" + role + "'}";
    }

    public static class Builder {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String profilePictureUrl;
        private String role;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder profilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; return this; }
        public Builder role(String role) { this.role = role; return this; }

        public ClientBasicInfoResponse build() {
            ClientBasicInfoResponse obj = new ClientBasicInfoResponse();
            obj.id = this.id;
            obj.firstName = this.firstName;
            obj.lastName = this.lastName;
            obj.email = this.email;
            obj.profilePictureUrl = this.profilePictureUrl;
            obj.role = this.role;
            return obj;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}

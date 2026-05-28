package com.project.tesi.dto.response;

import com.project.tesi.model.User;

/**
 * DTO intermedio che raggruppa il token JWT e l'entità User dopo l'autenticazione.
 */
public class AuthResult {

    private String token;
    private User user;


    private AuthResult(Builder b) {
        this.token = b.token;
        this.user = b.user;
    }

    public static Builder builder() { return new Builder(); }

    public String getToken() { return token; }
    public User getUser() { return user; }

    public static class Builder {
        private String token;
        private User user;

        public Builder token(String token) { this.token = token; return this; }
        public Builder user(User user) { this.user = user; return this; }

        public AuthResult build() { return new AuthResult(this); }
    }
}

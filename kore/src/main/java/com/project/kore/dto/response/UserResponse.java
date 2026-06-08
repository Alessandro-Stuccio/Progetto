package com.project.kore.dto.response;

import com.project.kore.enums.Role;

/**
 * Profilo completo di un utente. Alcuni campi valgono solo per certi ruoli: rating medio e numero
 * di clienti attivi per i professionisti, professionisti assegnati per il cliente.
 */
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String profilePictureUrl;
    private String assignedPtName;
    private String assignedNutritionistName;
    private Integer activeClientsCount;
    private Double averageRating;


    private UserResponse(Builder builder) {
        this.id = builder.id;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.role = builder.role;
        this.profilePictureUrl = builder.profilePictureUrl;
        this.assignedPtName = builder.assignedPtName;
        this.assignedNutritionistName = builder.assignedNutritionistName;
        this.activeClientsCount = builder.activeClientsCount;
        this.averageRating = builder.averageRating;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public String getAssignedPtName() { return assignedPtName; }
    public String getAssignedNutritionistName() { return assignedNutritionistName; }
    public Integer getActiveClientsCount() { return activeClientsCount; }
    public Double getAverageRating() { return averageRating; }

    public static class Builder {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private Role role;
        private String profilePictureUrl;
        private String assignedPtName;
        private String assignedNutritionistName;
        private Integer activeClientsCount;
        private Double averageRating;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder profilePictureUrl(String url) { this.profilePictureUrl = url; return this; }
        public Builder assignedPtName(String name) { this.assignedPtName = name; return this; }
        public Builder assignedNutritionistName(String name) { this.assignedNutritionistName = name; return this; }
        public Builder activeClientsCount(Integer count) { this.activeClientsCount = count; return this; }
        public Builder averageRating(Double rating) { this.averageRating = rating; return this; }

        public UserResponse build() { return new UserResponse(this); }
    }
}

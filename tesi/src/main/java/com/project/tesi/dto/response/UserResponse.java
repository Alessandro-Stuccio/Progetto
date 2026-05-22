package com.project.tesi.dto.response;

import com.project.tesi.enums.Role;

public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private Boolean active;
    private String profilePictureUrl;
    private String createdAt;
    private String assignedPtName;
    private String assignedNutritionistName;
    private String bio;
    private String specialization;
    private Integer activeClientsCount;
    private Double averageRating;

    private UserResponse() {}

    private UserResponse(Builder builder) {
        this.id = builder.id;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.role = builder.role;
        this.active = builder.active;
        this.profilePictureUrl = builder.profilePictureUrl;
        this.createdAt = builder.createdAt;
        this.assignedPtName = builder.assignedPtName;
        this.assignedNutritionistName = builder.assignedNutritionistName;
        this.bio = builder.bio;
        this.specialization = builder.specialization;
        this.activeClientsCount = builder.activeClientsCount;
        this.averageRating = builder.averageRating;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public Boolean getActive() { return active; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public String getCreatedAt() { return createdAt; }
    public String getAssignedPtName() { return assignedPtName; }
    public String getAssignedNutritionistName() { return assignedNutritionistName; }
    public String getBio() { return bio; }
    public String getSpecialization() { return specialization; }
    public Integer getActiveClientsCount() { return activeClientsCount; }
    public Double getAverageRating() { return averageRating; }

    public static class Builder {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private Role role;
        private Boolean active;
        private String profilePictureUrl;
        private String createdAt;
        private String assignedPtName;
        private String assignedNutritionistName;
        private String bio;
        private String specialization;
        private Integer activeClientsCount;
        private Double averageRating;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }
        public Builder profilePictureUrl(String url) { this.profilePictureUrl = url; return this; }
        public Builder createdAt(String createdAt) { this.createdAt = createdAt; return this; }
        public Builder assignedPtName(String name) { this.assignedPtName = name; return this; }
        public Builder assignedNutritionistName(String name) { this.assignedNutritionistName = name; return this; }
        public Builder bio(String bio) { this.bio = bio; return this; }
        public Builder specialization(String s) { this.specialization = s; return this; }
        public Builder activeClientsCount(Integer count) { this.activeClientsCount = count; return this; }
        public Builder averageRating(Double rating) { this.averageRating = rating; return this; }

        public UserResponse build() { return new UserResponse(this); }
    }
}

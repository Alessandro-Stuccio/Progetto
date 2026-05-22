package com.project.tesi.builder;

import com.project.tesi.enums.Role;
import java.time.LocalDateTime;
import com.project.tesi.model.*;


public interface UserBuilder {
    UserBuilder id(Long id);
    UserBuilder email(String email);
    UserBuilder password(String password);
    UserBuilder profilePicture(String profilePicture);
    UserBuilder firstName(String firstName);
    UserBuilder lastName(String lastName);
    UserBuilder role(Role role);
    UserBuilder professionalBio(String professionalBio);
    UserBuilder assignedPT(User assignedPT);
    UserBuilder assignedNutritionist(User assignedNutritionist);
    UserBuilder createdAt(LocalDateTime createdAt);
    UserBuilder updatedAt(LocalDateTime updatedAt);
    User build();
}

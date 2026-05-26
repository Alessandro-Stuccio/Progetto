package com.project.tesi.service;

import com.project.tesi.model.User;
import com.project.tesi.enums.Role;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Validated
public interface UserService {

    User getUserById(
            @NotNull(message = "l'id deve essere valorizzato")
            @Min(value = 1, message = "non esistono id negativi") Long id);

    Optional<User> findById(@NotNull @Min(1) Long id);

    User getUserByEmail(@NotNull String email);

    boolean existsByEmail(@NotNull String email);

    User save(@NotNull User user);

    List<User> findByRole(@NotNull Role role);

    List<User> findAll();

    long countByAssignedPT(@NotNull User pt);

    long countByAssignedNutritionist(@NotNull User nutritionist);

    List<User> findByAssignedPT(@NotNull User pt);

    List<User> findByAssignedNutritionist(@NotNull User nutritionist);

    void clearAssignedPT(@NotNull @Min(1) Long ptId);

    void clearAssignedNutritionist(@NotNull @Min(1) Long nutriId);

    String encodePassword(@NotNull String rawPassword);
}

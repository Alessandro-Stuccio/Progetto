package com.project.tesi.service;

import com.project.tesi.model.User;
import com.project.tesi.enums.Role;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface UserService {

    User getUserById(
            @NotNull(message = "l'id deve essere valorizzato")
            @Min(value = 1, message = "non esistono id negativi") Long id);

    User getUserByEmail(@NotNull String email);

    boolean existsByEmail(@NotNull String email);

    User save(@NotNull User user);

    List<User> findByRole(@NotNull Role role);

    List<User> findAll();

    long countByAssignedPT(@NotNull User pt);

    long countByAssignedNutritionist(@NotNull User nutritionist);
}

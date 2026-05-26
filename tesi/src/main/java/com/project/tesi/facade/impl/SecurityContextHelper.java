package com.project.tesi.facade.impl;

import com.project.tesi.model.User;
import com.project.tesi.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityContextHelper {

    private final UserService userService;

    public SecurityContextHelper(UserService userService) {
        this.userService = userService;
    }

    public Optional<User> getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return Optional.empty();
        }
        try {
            return Optional.of(userService.getUserByEmail(auth.getName()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

package com.project.tesi.service.impl;

import com.project.tesi.model.User;
import com.project.tesi.service.AuthService;
import com.project.tesi.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User resetPassword(String email, String newPassword) {
        User user = userService.getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userService.save(user);
    }
}

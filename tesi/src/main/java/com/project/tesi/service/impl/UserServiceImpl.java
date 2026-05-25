package com.project.tesi.service.impl;

import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.User;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente con email " + email + " non trovato."));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByAssignedPT(User pt) {
        return userRepository.countByAssignedPT(pt);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByAssignedNutritionist(User nutritionist) {
        return userRepository.countByAssignedNutritionist(nutritionist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByAssignedPT(User pt) {
        return userRepository.findByAssignedPT(pt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByAssignedNutritionist(User nutritionist) {
        return userRepository.findByAssignedNutritionist(nutritionist);
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}

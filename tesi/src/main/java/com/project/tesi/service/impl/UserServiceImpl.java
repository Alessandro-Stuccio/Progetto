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
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente con email " + email + " non trovato."));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public long countByAssignedPT(User pt) {
        return userRepository.countByAssignedPT(pt);
    }

    @Override
    public long countByAssignedNutritionist(User nutritionist) {
        return userRepository.countByAssignedNutritionist(nutritionist);
    }

    @Override
    public List<User> findByAssignedPT(User pt) {
        return userRepository.findByAssignedPT(pt);
    }

    @Override
    public List<User> findByAssignedNutritionist(User nutritionist) {
        return userRepository.findByAssignedNutritionist(nutritionist);
    }

    @Override
    public void clearAssignedPT(Long ptId) {
        userRepository.clearAssignedPT(ptId);
    }

    @Override
    public void clearAssignedNutritionist(Long nutriId) {
        userRepository.clearAssignedNutritionist(nutriId);
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}

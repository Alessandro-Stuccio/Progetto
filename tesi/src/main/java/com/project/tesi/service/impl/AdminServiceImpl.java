package com.project.tesi.service.impl;

import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.AdminService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public AdminServiceImpl(UserRepository userRepository, SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    // ────────────────────── Utenti ──────────────────────

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));
    }

    @Override
    public boolean existsUserByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean existsUserByEmailExcluding(String email, Long excludeId) {
        return userRepository.findByEmail(email)
                .filter(u -> !u.getId().equals(excludeId))
                .isPresent();
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));
        userRepository.delete(target);
    }

    // ────────────────────── Abbonamenti ──────────────────────

    @Override
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    public Subscription updateSubscriptionCredits(Long subscriptionId, int creditsPT, int creditsNutri) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Abbonamento", subscriptionId));

        sub.setCurrentCreditsPT(creditsPT);
        sub.setCurrentCreditsNutri(creditsNutri);
        return subscriptionRepository.save(sub);
    }
}

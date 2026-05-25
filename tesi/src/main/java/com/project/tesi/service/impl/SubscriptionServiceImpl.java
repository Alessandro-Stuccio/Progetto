package com.project.tesi.service.impl;

import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.subscription.SubscriptionNotFoundException;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.SubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
                                   UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Subscription getSubscriptionStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userId));

        return subscriptionRepository.findByUserAndActiveTrue(user)
                .orElseThrow(SubscriptionNotFoundException::new);
    }

    @Override
    @Transactional
    public Subscription save(Subscription sub) {
        return subscriptionRepository.save(sub);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findActiveByUser(User user) {
        return subscriptionRepository.findByUserAndActiveTrue(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findActiveByUserWithLock(User user) {
        return subscriptionRepository.findByUserAndActiveTrueWithLock(user);
    }
}

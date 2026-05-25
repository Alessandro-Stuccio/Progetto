package com.project.tesi.service.impl;

import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.Chat;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.ChatRepository;
import com.project.tesi.repository.DocumentRepository;
import com.project.tesi.repository.ReviewRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.repository.WeeklyScheduleRepository;
import com.project.tesi.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DocumentRepository documentRepository;
    private final ChatRepository chatRepository;
    private final ReviewRepository reviewRepository;
    private final SlotRepository slotRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            SubscriptionRepository subscriptionRepository,
                            DocumentRepository documentRepository,
                            ChatRepository chatRepository,
                            ReviewRepository reviewRepository,
                            SlotRepository slotRepository,
                            WeeklyScheduleRepository weeklyScheduleRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.documentRepository = documentRepository;
        this.chatRepository = chatRepository;
        this.reviewRepository = reviewRepository;
        this.slotRepository = slotRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
    }

    // ────────────────────── Utenti ──────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsUserByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsUserByEmailExcluding(String email, Long excludeId) {
        return userRepository.findByEmail(email)
                .filter(u -> !u.getId().equals(excludeId))
                .isPresent();
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));

        userRepository.clearAssignedPT(id);
        userRepository.clearAssignedNutritionist(id);

        safeDeleteRelatedData(id);

        subscriptionRepository.findByUserId(id).ifPresent(subscriptionRepository::delete);
        userRepository.delete(target);
    }

    // ────────────────────── Abbonamenti ──────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    @Transactional
    public Subscription updateSubscriptionCredits(Long subscriptionId, int creditsPT, int creditsNutri) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Abbonamento", subscriptionId));

        sub.setCurrentCreditsPT(creditsPT);
        sub.setCurrentCreditsNutri(creditsNutri);
        return subscriptionRepository.save(sub);
    }

    // ────────────────────── Utilità ──────────────────────

    private void safeDeleteRelatedData(Long userId) {
        slotRepository.clearBookingByUserId(userId);
        slotRepository.deleteByProfessionalId(userId);
        List<Chat> chats = chatRepository.findAllChatsByUserId(userId);
        chatRepository.deleteAll(chats);
        reviewRepository.deleteByUserId(userId);
        weeklyScheduleRepository.deleteByProfessionalId(userId);
        documentRepository.deleteByUserId(userId);
    }
}

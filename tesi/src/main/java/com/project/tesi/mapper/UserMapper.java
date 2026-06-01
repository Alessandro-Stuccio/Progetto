package com.project.tesi.mapper;

import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ProfessionalSummaryDTO;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.model.User;
import com.project.tesi.repository.ReviewRepository;
import com.project.tesi.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converte gli utenti tra entità e DTO. Per i professionisti aggiunge alla risposta
 * la media voti e il numero di clienti attivi, leggendoli dai repository.
 */
@Component
public class UserMapper {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public UserMapper(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    // Risposta completa: ai clienti aggiunge i nomi di PT e nutrizionista assegnati,
    // ai professionisti la media voti e il conteggio dei clienti attivi.
    public UserResponse toUserResponse(User user) {
        Double avgRating = null;
        Integer clientsCount = null;

        // Solo i professionisti hanno rating e clienti da calcolare.
        if (user.getRole() == Role.PERSONAL_TRAINER || user.getRole() == Role.NUTRITIONIST) {
            avgRating = reviewRepository.getAverageRating(user.getId());
            if (avgRating == null) avgRating = 0.0;
            if (user.getRole() == Role.PERSONAL_TRAINER) {
                clientsCount = (int) userRepository.countByAssignedPTAndDeletedFalse(user);
            } else {
                clientsCount = (int) userRepository.countByAssignedNutritionistAndDeletedFalse(user);
            }
        }

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .profilePictureUrl(user.getProfilePicture())
                .assignedPtName(user.getAssignedPT() != null ?
                        user.getAssignedPT().getFullName() : null)
                .assignedNutritionistName(user.getAssignedNutritionist() != null ?
                        user.getAssignedNutritionist().getFullName() : null)
                .activeClientsCount(clientsCount)
                .averageRating(avgRating)
                .build();
    }

    // Versione leggera senza accessi al DB: per le viste admin/moderator
    // rating e conteggio clienti non servono.
    public UserResponse toAdminResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .profilePictureUrl(user.getProfilePicture())
                .assignedPtName(user.getAssignedPT() != null ?
                        user.getAssignedPT().getFullName() : null)
                .assignedNutritionistName(user.getAssignedNutritionist() != null ?
                        user.getAssignedNutritionist().getFullName() : null)
                .build();
    }

    public List<UserResponse> toAdminResponse(List<User> user) {
        return user==null?new ArrayList<>():user.stream().map(this::toAdminResponse).toList();
    }

    public User toUser(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(request.password())
                .profilePicture(request.profilePicture())
                .role(Role.CLIENT)
                .build();
    }

    public ClientBasicInfoResponse toBasicInfoResponse(User user) {
        return ClientBasicInfoResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePicture())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }

    public ProfessionalSummaryDTO toProfessionalSummary(User pro) {
        return ProfessionalSummaryDTO.builder()
                .id(pro.getId())
                .fullName(pro.getFullName())
                .role(pro.getRole())
                .build();
    }
}
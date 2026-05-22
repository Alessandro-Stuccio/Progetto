package com.project.tesi.mapper;

import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.model.User;
import com.project.tesi.repository.ReviewRepository;
import com.project.tesi.repository.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Mapper per la conversione bidirezionale tra l'entità {@link User} e i suoi DTO.
 *
 * <ul>
 *   <li>{@code toUserResponse} — User → UserResponse (con dati aggiuntivi dal DB)</li>
 *   <li>{@code toUser} — RegisterRequest → User (per la registrazione)</li>
 * </ul>
 *
 * Per i professionisti, arricchisce la risposta con la media voti (dal {@link ReviewRepository})
 * e il conteggio clienti attivi (dal {@link UserRepository}).
 */
@Component
public class UserMapper {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public UserMapper(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * Converte un'entità User nel DTO di risposta, arricchendo i dati:
     * <ul>
     *   <li>Per i clienti: nome del PT e Nutrizionista assegnati</li>
     *   <li>Per i professionisti: media voti e numero clienti attivi</li>
     * </ul>
     *
     * @param user l'entità utente
     * @return il DTO di risposta con i dati completi
     */
    public UserResponse toUserResponse(User user) {
        Double avgRating = null;
        Integer clientsCount = null;

        // Arricchimento dati per i professionisti
        if (user.getRole() == Role.PERSONAL_TRAINER || user.getRole() == Role.NUTRITIONIST) {
            avgRating = reviewRepository.getAverageRating(user.getId());
            if (avgRating == null) avgRating = 0.0;
            if (user.getRole() == Role.PERSONAL_TRAINER) {
                clientsCount = (int) userRepository.countByAssignedPT(user);
            } else {
                clientsCount = (int) userRepository.countByAssignedNutritionist(user);
            }
        }

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .assignedPtName(user.getAssignedPT() != null ?
                        user.getAssignedPT().getFullName() : null)
                .assignedNutritionistName(user.getAssignedNutritionist() != null ?
                        user.getAssignedNutritionist().getFullName() : null)
                .activeClientsCount(clientsCount)
                .averageRating(avgRating)
                .build();
    }

    /**
     * Versione leggera senza enrichment da DB; usata nei contesti admin/moderator
     * dove il rating e il conteggio clienti vengono omessi per semplicità.
     */
    public UserResponse toAdminResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .assignedPtName(user.getAssignedPT() != null ?
                        user.getAssignedPT().getFullName() : null)
                .assignedNutritionistName(user.getAssignedNutritionist() != null ?
                        user.getAssignedNutritionist().getFullName() : null)
                .build();
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
}
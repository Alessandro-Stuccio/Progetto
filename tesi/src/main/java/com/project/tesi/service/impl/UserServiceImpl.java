package com.project.tesi.service.impl;

import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.CustomResourceNotFoundException;
import com.project.tesi.model.User;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementazione di {@link UserService}.
 * Gestisce gli utenti tramite {@link com.project.tesi.repository.UserRepository}.
 * La password viene codificata con BCrypt prima del salvataggio tramite
 * {@link org.springframework.security.crypto.password.PasswordEncoder}.
 */
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
                .orElseThrow(() -> new CustomResourceNotFoundException("Utente", id));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new CustomResourceNotFoundException("Utente con email " + email + " non trovato."));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email).isPresent();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findByRole(Role role) {
        return userRepository.findByRoleAndDeletedFalse(role);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAllByDeletedFalse();
    }

    @Override
    public long countByAssignedPT(User pt) {
        return userRepository.countByAssignedPTAndDeletedFalse(pt);
    }

    @Override
    public long countByAssignedNutritionist(User nutritionist) {
        return userRepository.countByAssignedNutritionistAndDeletedFalse(nutritionist);
    }

    @Override
    public List<User> findByAssignedPT(User pt) {
        return userRepository.findByAssignedPTAndDeletedFalse(pt);
    }

    @Override
    public List<User> findByAssignedNutritionist(User nutritionist) {
        return userRepository.findByAssignedNutritionistAndDeletedFalse(nutritionist);
    }

    /**
     * Verifica l'unicità dell'email escludendo l'utente corrente.
     * Usato durante l'aggiornamento del profilo per impedire la duplicazione
     * dell'email con un altro account già esistente.
     *
     * @param email     email da verificare
     * @param excludeId id dell'utente da escludere dal controllo
     * @return {@code true} se l'email è già usata da un altro utente attivo
     */
    @Override
    public boolean existsUserByEmailExcluding(String email, Long excludeId) {
        return userRepository.findByEmailAndIdIsNotAndDeletedFalse(email, excludeId).isPresent();
    }

    /**
     * Soft delete dell'utente: imposta {@code deleted=true} senza rimuovere fisicamente
     * il record dal database. Se l'utente è un Personal Trainer o un Nutrizionista,
     * azzera il riferimento nei client ad esso assegnati.
     *
     * @param id id dell'utente da eliminare
     * @throws CustomResourceNotFoundException se l'utente non esiste
     */
    @Override
    public void deleteUser(Long id) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Utente", id));
        target.setDeleted(true);
        userRepository.save(target);

        if (target.getRole() == Role.PERSONAL_TRAINER) {
            userRepository.clearAssignedPT(id);
        } else if (target.getRole() == Role.NUTRITIONIST) {
            userRepository.clearAssignedNutritionist(id);
        }
    }

    /**
     * Codifica la password raw con {@code BCryptPasswordEncoder}.
     *
     * @param rawPassword password in chiaro
     * @return hash BCrypt della password
     */
    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}

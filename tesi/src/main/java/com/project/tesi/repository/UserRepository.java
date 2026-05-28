package com.project.tesi.repository;

import com.project.tesi.model.User;
import com.project.tesi.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per l'accesso ai dati dell'entità {@link User}.
 *
 * Estende {@link JpaRepository} che fornisce automaticamente le operazioni
 * CRUD di base (findAll, findById, save, delete, ecc.).
 * I metodi aggiuntivi usano le query derivate di Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByEmailAndIdIsNotAndDeletedFalse(String email, Long id);

    List<User> findAllByDeletedFalse();

    List<User> findByRole(Role role);

    List<User> findByRoleAndDeletedFalse(Role role);

    long countByAssignedPT(User pt);

    long countByAssignedPTAndDeletedFalse(User pt);

    long countByAssignedNutritionist(User nutritionist);

    long countByAssignedNutritionistAndDeletedFalse(User nutritionist);

    List<User> findByAssignedPT(User pt);

    List<User> findByAssignedPTAndDeletedFalse(User pt);

    List<User> findByAssignedNutritionist(User nutritionist);

    List<User> findByAssignedNutritionistAndDeletedFalse(User nutritionist);

    @Modifying
    @Query("UPDATE User u SET u.assignedPT = null WHERE u.assignedPT.id = :ptId")
    void clearAssignedPT(@Param("ptId") Long ptId);

    @Modifying
    @Query("UPDATE User u SET u.assignedNutritionist = null WHERE u.assignedNutritionist.id = :nutriId")
    void clearAssignedNutritionist(@Param("nutriId") Long nutriId);
}

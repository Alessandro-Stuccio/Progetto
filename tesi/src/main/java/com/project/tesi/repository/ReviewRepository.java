package com.project.tesi.repository;

import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Recensioni dei professionisti: esistenza, elenco per professionista e media voti.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Serve a far rispettare il vincolo di unicità: una sola recensione per coppia cliente-professionista.
    boolean existsByClientIdAndProfessionalId(Long clientId, Long professionalId);

    // Media dei voti calcolata nel database; null se il professionista non ha ancora recensioni.
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.professional.id = :profId")
    Double getAverageRating(@Param("profId") Long profId);

    List<Review> findByProfessional(User professional);

}
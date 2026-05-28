package com.project.tesi.repository;

import com.project.tesi.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository per l'accesso ai dati dell'entità {@link AuditLog}.
 * Fornisce solo le operazioni CRUD standard di {@link JpaRepository}.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}

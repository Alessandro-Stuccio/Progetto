package com.project.kore.config;

import com.project.kore.model.AuditLog;
import com.project.kore.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Persistenza asincrona dei record di audit. È un bean separato da
 * {@link AuditInterceptor} perché @Async funziona solo se la chiamata passa dal
 * proxy Spring: invocato dall'interno della stessa classe verrebbe eseguito in
 * modo sincrono sul thread della richiesta.
 */
@Component
public class AuditLogWriter {

    private static final Logger log = LoggerFactory.getLogger(AuditLogWriter.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogWriter(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async("emailTaskExecutor")
    public void persist(String userIdentity, String method, String path,
                        String ip, int httpStatus, String requestBody) {
        try {
            AuditLog entry = new AuditLog();
            entry.setLoggedAt(LocalDateTime.now());
            entry.setUserIdentity(userIdentity);
            entry.setHttpMethod(method);
            entry.setHttpPath(path);
            entry.setIpAddress(ip);
            entry.setHttpStatus(httpStatus);
            entry.setRequestBody(requestBody);
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("Audit log fallito per {} {}: {}", method, path, e.getMessage());
        }
    }
}

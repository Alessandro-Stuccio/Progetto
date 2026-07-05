package com.project.kore.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Per ogni richiesta HTTP salva in modo asincrono un record nell'audit log:
 * utente, metodo, path, IP, status e, solo per POST/PUT/PATCH, il body troncato
 * a 2000 caratteri. Il body è leggibile perché {@link RequestBodyCachingFilter}
 * avvolge la request a monte. La scrittura vera e propria è delegata ad
 * {@link AuditLogWriter} così l'@Async passa dal proxy Spring.
 */
@Component
public class AuditInterceptor implements HandlerInterceptor {

    private static final int MAX_BODY_LENGTH = 2000;
    private static final Set<String> BODY_METHODS = Set.of("POST", "PUT", "PATCH");

    private final AuditLogWriter auditLogWriter;

    public AuditInterceptor(AuditLogWriter auditLogWriter) {
        this.auditLogWriter = auditLogWriter;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String userIdentity = extractUserIdentity();
        String method       = request.getMethod();
        String path         = request.getRequestURI();
        String ip           = request.getRemoteAddr();
        int    status       = response.getStatus();
        String body         = extractBody(request, method);
        auditLogWriter.persist(userIdentity, method, path, ip, status, body);
    }

    private String extractBody(HttpServletRequest request, String method) {
        if (!BODY_METHODS.contains(method)) {
            return null;
        }
        // Solo body testuali: i multipart (upload PDF) non vengono avvolti dal filtro
        // e comunque non avrebbe senso salvare byte binari nell'audit log.
        String contentType = request.getContentType();
        if (contentType == null || !isTextual(contentType)) {
            return null;
        }
        if (request instanceof ContentCachingRequestWrapper wrapped) {
            byte[] bytes = wrapped.getContentAsByteArray();
            if (bytes.length == 0) {
                return null;
            }
            String raw = new String(bytes, StandardCharsets.UTF_8);
            return raw.length() > MAX_BODY_LENGTH ? raw.substring(0, MAX_BODY_LENGTH) + "…" : raw;
        }
        return null;
    }

    private boolean isTextual(String contentType) {
        String lower = contentType.toLowerCase();
        return lower.contains("json") || lower.contains("xml")
                || lower.startsWith("text/") || lower.contains("x-www-form-urlencoded");
    }

    private String extractUserIdentity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "anonymous";
    }
}

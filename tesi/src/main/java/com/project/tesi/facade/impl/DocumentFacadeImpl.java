package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.facade.ActivityFeedFacade;
import com.project.tesi.facade.DocumentFacade;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.DocumentService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Implementazione del facade per la gestione dei documenti.
 */
@Component
public class DocumentFacadeImpl implements DocumentFacade {

    private final DocumentService documentService;
    private final ActivityFeedFacade activityFeedFacade;
    private final UserRepository userRepository;

    public DocumentFacadeImpl(DocumentService documentService,
                              ActivityFeedFacade activityFeedFacade,
                              UserRepository userRepository) {
        this.documentService = documentService;
        this.activityFeedFacade = activityFeedFacade;
        this.userRepository = userRepository;
    }

    @Override
    public DocumentUploadResponse uploadDocumentWithValidation(MultipartFile file, Long clientId, Long uploaderId, String type) {
        DocumentUploadResponse result = documentService.uploadDocumentWithValidation(file, clientId, uploaderId, type);
        activityFeedFacade.logDocumentUploaded(clientId, uploaderId, type);
        return result;
    }

    @Override
    public Document getDocumentById(Long id) {
        return documentService.getDocumentById(id);
    }

    @Override
    public byte[] downloadDocument(Long id) {
        return documentService.downloadDocument(id);
    }

    @Override
    public List<DocumentResponse> getUserDocumentsDto(Long userId) {
        return documentService.getUserDocumentsDto(userId);
    }

    @Override
    public List<DocumentResponse> getUserDocumentsByTypeDto(Long userId, String type) {
        return documentService.getUserDocumentsByTypeDto(userId, type);
    }

    @Override
    public void deleteDocument(Long id, Long callerId) {
        Document doc = documentService.getDocumentById(id);
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new UnauthorizedAccessException("Utente non trovato"));
        boolean isOwner = doc.getOwner() != null && doc.getOwner().getId().equals(callerId);
        boolean isUploader = doc.getUploadedBy() != null && doc.getUploadedBy().getId().equals(callerId);
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isOwner && !isUploader && !isPrivileged) {
            throw new UnauthorizedAccessException("Non sei autorizzato a eliminare questo documento");
        }
        documentService.deleteDocument(id);
    }

    @Override
    public byte[] downloadDocumentSecure(Long id, Long callerId) {
        Document doc = documentService.getDocumentById(id);
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new UnauthorizedAccessException("Utente non trovato"));
        boolean isOwner = doc.getOwner() != null && doc.getOwner().getId().equals(callerId);
        boolean isUploader = doc.getUploadedBy() != null && doc.getUploadedBy().getId().equals(callerId);
        boolean isProfessional = caller.getRole() == Role.PERSONAL_TRAINER || caller.getRole() == Role.NUTRITIONIST;
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isOwner && !isUploader && !isProfessional && !isPrivileged) {
            throw new UnauthorizedAccessException("Non sei autorizzato a scaricare questo documento");
        }
        return documentService.downloadDocument(id);
    }

    @Override
    public List<DocumentResponse> getUserDocumentsDtoSecure(Long targetUserId, Long callerId) {
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new UnauthorizedAccessException("Utente non trovato"));
        boolean isSelf = callerId.equals(targetUserId);
        boolean isProfessional = caller.getRole() == Role.PERSONAL_TRAINER || caller.getRole() == Role.NUTRITIONIST;
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isSelf && !isProfessional && !isPrivileged) {
            throw new UnauthorizedAccessException("Non sei autorizzato a visualizzare questi documenti");
        }
        return documentService.getUserDocumentsDto(targetUserId);
    }

    @Override
    public List<DocumentResponse> getUserDocumentsByTypeDtoSecure(Long targetUserId, String type, Long callerId) {
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new UnauthorizedAccessException("Utente non trovato"));
        boolean isSelf = callerId.equals(targetUserId);
        boolean isProfessional = caller.getRole() == Role.PERSONAL_TRAINER || caller.getRole() == Role.NUTRITIONIST;
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isSelf && !isProfessional && !isPrivileged) {
            throw new UnauthorizedAccessException("Non sei autorizzato a visualizzare questi documenti");
        }
        return documentService.getUserDocumentsByTypeDto(targetUserId, type);
    }

    @Override
    public UpdatedNotesResponse updateNotes(Long id, String notes, Long callerId) {
        Document doc = documentService.getDocumentById(id);
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new UnauthorizedAccessException("Utente non trovato"));
        boolean isOwner      = doc.getOwner() != null && doc.getOwner().getId().equals(callerId);
        boolean isUploader   = doc.getUploadedBy() != null && doc.getUploadedBy().getId().equals(callerId);
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isOwner && !isUploader && !isPrivileged) {
            throw new UnauthorizedAccessException("Non sei autorizzato a modificare le note di questo documento");
        }
        return documentService.updateNotes(id, notes);
    }
}

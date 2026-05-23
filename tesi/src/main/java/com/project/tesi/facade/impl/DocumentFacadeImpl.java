package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.facade.ActivityFeedFacade;
import com.project.tesi.facade.DocumentFacade;
import com.project.tesi.mapper.DocumentMapper;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import com.project.tesi.service.DocumentService;
import com.project.tesi.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class DocumentFacadeImpl implements DocumentFacade {

    private final DocumentService documentService;
    private final ActivityFeedFacade activityFeedFacade;
    private final UserService userService;
    private final DocumentMapper documentMapper;

    public DocumentFacadeImpl(DocumentService documentService,
                              ActivityFeedFacade activityFeedFacade,
                              UserService userService,
                              DocumentMapper documentMapper) {
        this.documentService = documentService;
        this.activityFeedFacade = activityFeedFacade;
        this.userService = userService;
        this.documentMapper = documentMapper;
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
        return documentMapper.toResponseList(documentService.getUserDocuments(userId));
    }

    @Override
    public List<DocumentResponse> getUserDocumentsByTypeDto(Long userId, String type) {
        return documentMapper.toResponseList(documentService.getUserDocumentsByType(userId, type));
    }

    @Override
    public void deleteDocument(Long id, Long callerId) {
        Document doc = documentService.getDocumentById(id);
        User caller = userService.getUserById(callerId);
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
        User caller = userService.getUserById(callerId);
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
        User caller = userService.getUserById(callerId);
        boolean isSelf = callerId.equals(targetUserId);
        boolean isProfessional = caller.getRole() == Role.PERSONAL_TRAINER || caller.getRole() == Role.NUTRITIONIST;
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isSelf && !isProfessional && !isPrivileged) {
            throw new UnauthorizedAccessException("Non sei autorizzato a visualizzare questi documenti");
        }
        return documentMapper.toResponseList(documentService.getUserDocuments(targetUserId));
    }

    @Override
    public List<DocumentResponse> getUserDocumentsByTypeDtoSecure(Long targetUserId, String type, Long callerId) {
        User caller = userService.getUserById(callerId);
        boolean isSelf = callerId.equals(targetUserId);
        boolean isProfessional = caller.getRole() == Role.PERSONAL_TRAINER || caller.getRole() == Role.NUTRITIONIST;
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isSelf && !isProfessional && !isPrivileged) {
            throw new UnauthorizedAccessException("Non sei autorizzato a visualizzare questi documenti");
        }
        return documentMapper.toResponseList(documentService.getUserDocumentsByType(targetUserId, type));
    }

    @Override
    public UpdatedNotesResponse updateNotes(Long id, String notes, Long callerId) {
        Document doc = documentService.getDocumentById(id);
        User caller = userService.getUserById(callerId);
        boolean isOwner = doc.getOwner() != null && doc.getOwner().getId().equals(callerId);
        boolean isUploader = doc.getUploadedBy() != null && doc.getUploadedBy().getId().equals(callerId);
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isOwner && !isUploader && !isPrivileged) {
            throw new UnauthorizedAccessException("Non sei autorizzato a modificare le note di questo documento");
        }
        return documentService.updateNotes(id, notes);
    }
}

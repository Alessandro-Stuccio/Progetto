package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.exception.document.InvalidFileException;
import com.project.tesi.facade.DocumentFacade;
import com.project.tesi.service.ActivityFeedService;
import com.project.tesi.mapper.DocumentMapper;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import com.project.tesi.service.DocumentService;
import com.project.tesi.service.FileStorageService;
import com.project.tesi.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class DocumentFacadeImpl implements DocumentFacade {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final ActivityFeedService activityFeedService;
    private final UserService userService;
    private final DocumentMapper documentMapper;

    public DocumentFacadeImpl(DocumentService documentService,
                              FileStorageService fileStorageService,
                              ActivityFeedService activityFeedService,
                              UserService userService,
                              DocumentMapper documentMapper) {
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
        this.activityFeedService = activityFeedService;
        this.userService = userService;
        this.documentMapper = documentMapper;
    }

    @Override
    @Transactional
    public DocumentUploadResponse uploadDocumentWithValidation(MultipartFile file, Long clientId, Long uploaderId, String type) {
        User uploader = userService.getUserById(uploaderId);

        if (uploader.getRole() == Role.PERSONAL_TRAINER && !"WORKOUT_PLAN".equals(type)) {
            throw new InvalidFileException("Il Personal Trainer può caricare solo schede di allenamento.");
        }
        if (uploader.getRole() == Role.NUTRITIONIST && !"DIET_PLAN".equals(type)) {
            throw new InvalidFileException("Il Nutrizionista può caricare solo piani alimentari.");
        }

        String filePath = fileStorageService.store(file);

        Document doc;
        try {
            doc = documentService.uploadDocument(filePath, file.getOriginalFilename(),
                    file.getContentType(), type, clientId, uploaderId);
        } catch (Exception e) {
            fileStorageService.delete(filePath);
            throw e;
        }

        activityFeedService.logDocumentUploaded(clientId, uploaderId, type);
        return new DocumentUploadResponse(doc.getId(), doc.getFileName(), doc.getType().name(), doc.getUploadDate().toString());
    }

    @Override
    @Transactional(readOnly = true)
    public Document getDocumentById(Long id) {
        return documentService.getDocumentById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long id) {
        Document doc = documentService.getDocumentById(id);
        return fileStorageService.load(doc.getFilePath());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getUserDocumentsDto(Long userId) {
        return documentMapper.toResponseList(documentService.getUserDocuments(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getUserDocumentsByTypeDto(Long userId, String type) {
        return documentMapper.toResponseList(documentService.getUserDocumentsByType(userId, type));
    }

    @Override
    @Transactional
    public void deleteDocument(Long id, Long callerId) {
        Document doc = documentService.getDocumentById(id);
        User caller = userService.getUserById(callerId);
        boolean isOwner = doc.getOwner() != null && doc.getOwner().getId().equals(callerId);
        boolean isUploader = doc.getUploadedBy() != null && doc.getUploadedBy().getId().equals(callerId);
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isOwner && !isUploader && !isPrivileged) {
            throw new UnauthorizedAccessException("Non sei autorizzato a eliminare questo documento");
        }
        String filePath = doc.getFilePath();
        documentService.deleteDocument(id);
        fileStorageService.delete(filePath);
    }

    @Override
    @Transactional(readOnly = true)
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
        return fileStorageService.load(doc.getFilePath());
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional
    public UpdatedNotesResponse updateNotes(Long id, String notes, Long callerId) {
        Document doc = documentService.getDocumentById(id);
        User caller = userService.getUserById(callerId);
        boolean isOwner = doc.getOwner() != null && doc.getOwner().getId().equals(callerId);
        boolean isUploader = doc.getUploadedBy() != null && doc.getUploadedBy().getId().equals(callerId);
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isOwner && !isUploader && !isPrivileged) {
            throw new UnauthorizedAccessException("Non sei autorizzato a modificare le note di questo documento");
        }
        return documentMapper.toUpdatedNotesResponse(documentService.updateNotes(id, notes));
    }
}

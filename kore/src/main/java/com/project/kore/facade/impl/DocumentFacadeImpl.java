package com.project.kore.facade.impl;

import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.DocumentUploadResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import com.project.kore.exception.document.InvalidFileException;
import com.project.kore.facade.DocumentFacade;
import com.project.kore.service.*;
import com.project.kore.mapper.DocumentMapper;
import com.project.kore.model.Document;
import com.project.kore.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Upload, download e accesso ai documenti, con controllo dei permessi prima di ogni operazione.
 */
@Component
public class DocumentFacadeImpl implements DocumentFacade {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final DocumentMapper documentMapper;

    public DocumentFacadeImpl(DocumentService documentService,
                              FileStorageService fileStorageService,
                              UserService userService,
                              DocumentMapper documentMapper) {
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.documentMapper = documentMapper;
    }

    /**
     * Carica un documento dopo aver verificato che il tipo sia coerente col ruolo di chi lo carica
     * (un PT carica solo schede, un nutrizionista solo diete, ecc.). Se la scrittura del record
     * fallisce il file viene cancellato, così da non lasciare orfani.
     */
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
        if (uploader.getRole() == Role.INSURANCE_MANAGER && !"INSURANCE_POLICE".equals(type)) {
            throw new InvalidFileException("L'Insurance Manager può caricare solo polizze assicurative.");
        }

        User client = userService.getUserById(clientId);
        String filePath = fileStorageService.store(file);

        Document doc;
        try {
            doc = documentService.uploadDocument(filePath, file.getOriginalFilename(),
                    file.getContentType(), type, client, uploader);
        } catch (Exception e) {
            fileStorageService.delete(filePath);
            throw e;
        }

        return DocumentUploadResponse.builder()
                .id(doc.getId())
                .fileName(doc.getFileName())
                .type(doc.getType().name())
                .uploadDate(doc.getUploadDate().toString())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Document getDocumentById(Long id) {
        return documentService.getDocumentById(id);
    }

    /**
     * Elimina un documento (record e file): possono farlo solo chi l'ha caricato, un admin o un moderatore.
     */
    @Override
    @Transactional
    public void deleteDocument(Long id, Long callerId) {
        Document doc = documentService.getDocumentById(id);
        User caller = userService.getUserById(callerId);
        boolean isUploader = doc.getUploadedBy() != null && doc.getUploadedBy().getId().equals(callerId);
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isUploader && !isPrivileged) {
            throw new AccessDeniedException("Non sei autorizzato a eliminare questo documento");
        }
        String filePath = doc.getFilePath();
        documentService.deleteDocument(id);
        fileStorageService.delete(filePath);
    }

    /**
     * Scarica un documento. Possono accedervi il proprietario, chi l'ha caricato, il professionista
     * assegnato al proprietario (PT o nutrizionista), gli admin e i moderatori.
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocumentSecure(Long id, Long callerId) {
        Document doc = documentService.getDocumentById(id);
        User caller = userService.getUserById(callerId);
        boolean isOwner = doc.getOwner() != null && doc.getOwner().getId().equals(callerId);
        boolean isUploader = doc.getUploadedBy() != null && doc.getUploadedBy().getId().equals(callerId);
        boolean isAssignedPT = doc.getOwner() != null
                && caller.getRole() == Role.PERSONAL_TRAINER
                && doc.getOwner().getAssignedPT() != null
                && doc.getOwner().getAssignedPT().getId().equals(callerId);
        boolean isAssignedNutri = doc.getOwner() != null
                && caller.getRole() == Role.NUTRITIONIST
                && doc.getOwner().getAssignedNutritionist() != null
                && doc.getOwner().getAssignedNutritionist().getId().equals(callerId);
        boolean isAssignedProfessional = isAssignedPT || isAssignedNutri;
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isOwner && !isUploader && !isAssignedProfessional && !isPrivileged) {
            throw new AccessDeniedException("Non sei autorizzato a scaricare questo documento");
        }
        return fileStorageService.load(doc.getFilePath());
    }

    /**
     * Documenti di un utente, accessibili all'utente stesso, al suo professionista assegnato,
     * agli admin e ai moderatori.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getUserDocumentsDtoSecure(Long targetUserId, Long callerId) {
        User caller = userService.getUserById(callerId);
        boolean isSelf = callerId.equals(targetUserId);
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        User target = userService.getUserById(targetUserId);
        boolean isAssignedPT = caller.getRole() == Role.PERSONAL_TRAINER
                && target.getAssignedPT() != null
                && target.getAssignedPT().getId().equals(callerId);
        boolean isAssignedNutri = caller.getRole() == Role.NUTRITIONIST
                && target.getAssignedNutritionist() != null
                && target.getAssignedNutritionist().getId().equals(callerId);
        boolean isAssignedProfessional = isAssignedPT || isAssignedNutri;
        if (!isSelf && !isAssignedProfessional && !isPrivileged) {
            throw new AccessDeniedException("Non sei autorizzato a visualizzare questi documenti");
        }
        return documentMapper.toResponseList(documentService.getUserDocuments(target));
    }

    // Come getUserDocumentsDtoSecure, ma filtrando per tipo di documento (stessa politica di accesso).
    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getUserDocumentsByTypeDtoSecure(Long targetUserId, String type, Long callerId) {
        User caller = userService.getUserById(callerId);
        boolean isSelf = callerId.equals(targetUserId);
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        User target = userService.getUserById(targetUserId);
        boolean isAssignedPT = caller.getRole() == Role.PERSONAL_TRAINER
                && target.getAssignedPT() != null
                && target.getAssignedPT().getId().equals(callerId);
        boolean isAssignedNutri = caller.getRole() == Role.NUTRITIONIST
                && target.getAssignedNutritionist() != null
                && target.getAssignedNutritionist().getId().equals(callerId);
        boolean isAssignedProfessional = isAssignedPT || isAssignedNutri;
        if (!isSelf && !isAssignedProfessional && !isPrivileged) {
            throw new AccessDeniedException("Non sei autorizzato a visualizzare questi documenti");
        }
        return documentMapper.toResponseList(documentService.getUserDocumentsByType(target, type));
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
            throw new AccessDeniedException("Non sei autorizzato a modificare le note di questo documento");
        }
        return documentMapper.toUpdatedNotesResponse(documentService.updateNotes(id, notes));
    }
}

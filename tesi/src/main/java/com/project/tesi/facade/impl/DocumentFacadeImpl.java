package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.exception.document.InvalidFileException;
import com.project.tesi.facade.DocumentFacade;
import com.project.tesi.service.*;
import com.project.tesi.mapper.DocumentMapper;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Implementazione di {@link com.project.tesi.facade.DocumentFacade}.
 * Gestisce upload, download e accesso sicuro ai documenti verificando
 * i permessi del chiamante prima di ogni operazione su file o record DB.
 */
@Component
public class DocumentFacadeImpl implements DocumentFacade {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final DocumentMapper documentMapper;
    private final SlotService slotService;

    public DocumentFacadeImpl(DocumentService documentService,
                              FileStorageService fileStorageService,
                              UserService userService,
                              DocumentMapper documentMapper, SlotService slotService) {
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.documentMapper = documentMapper;
        this.slotService = slotService;
    }

    /**
     * Carica un documento dopo averne validato il tipo in base al ruolo dell'uploader.
     * Salva il file su filesystem tramite {@link com.project.tesi.service.FileStorageService};
     * se la creazione del record DB fallisce, il file viene rimosso per evitare orfani.
     *
     * @param file       file da caricare
     * @param clientId   ID dell'utente a cui appartiene il documento
     * @param uploaderId ID dell'utente che esegue l'upload
     * @param type       tipo documento (es. {@code WORKOUT_PLAN}, {@code DIET_PLAN}, {@code INSURANCE_POLICE})
     * @return DTO con i metadati del documento caricato
     * @throws com.project.tesi.exception.document.InvalidFileException se il tipo non corrisponde al ruolo dell'uploader
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
     * Elimina un documento verificando i permessi del chiamante.
     * Solo l'uploader originale, un {@code ADMIN} o un {@code MODERATOR}
     * possono procedere; altrimenti viene lanciata
     * {@link com.project.tesi.exception.common.UnauthorizedAccessException}.
     * Rimuove prima il record DB e poi il file da filesystem.
     *
     * @param id       ID del documento da eliminare
     * @param callerId ID dell'utente che richiede l'eliminazione
     * @throws com.project.tesi.exception.common.UnauthorizedAccessException se il chiamante non è autorizzato
     */
    @Override
    @Transactional
    public void deleteDocument(Long id, Long callerId) {
        Document doc = documentService.getDocumentById(id);
        User caller = userService.getUserById(callerId);
        boolean isUploader = doc.getUploadedBy() != null && doc.getUploadedBy().getId().equals(callerId);
        boolean isPrivileged = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MODERATOR;
        if (!isUploader && !isPrivileged) {
            throw new UnauthorizedAccessException("Non sei autorizzato a eliminare questo documento");
        }
        String filePath = doc.getFilePath();
        documentService.deleteDocument(id);
        fileStorageService.delete(filePath);
    }

    /**
     * Scarica i byte di un documento verificando i permessi del chiamante.
     * Accesso consentito a: owner del documento, uploader, professionista assegnato
     * al client owner (PT o Nutrizionista), {@code ADMIN} e {@code MODERATOR}.
     *
     * @param id       ID del documento da scaricare
     * @param callerId ID dell'utente che richiede il download
     * @return array di byte del file
     * @throws com.project.tesi.exception.common.UnauthorizedAccessException se il chiamante non è autorizzato
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
            throw new UnauthorizedAccessException("Non sei autorizzato a scaricare questo documento");
        }
        return fileStorageService.load(doc.getFilePath());
    }

    /**
     * Restituisce tutti i documenti di un utente target previa verifica dei permessi.
     * Accesso consentito a: l'utente stesso, il professionista assegnato,
     * {@code ADMIN} e {@code MODERATOR}.
     *
     * @param targetUserId ID dell'utente di cui recuperare i documenti
     * @param callerId     ID dell'utente che effettua la richiesta
     * @return lista di DTO dei documenti
     * @throws com.project.tesi.exception.common.UnauthorizedAccessException se il chiamante non è autorizzato
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
            throw new UnauthorizedAccessException("Non sei autorizzato a visualizzare questi documenti");
        }
        return documentMapper.toResponseList(documentService.getUserDocuments(target));
    }

    /**
     * Restituisce i documenti di un tipo specifico di un utente target previa verifica dei permessi.
     * Stessa politica di accesso di {@link #getUserDocumentsDtoSecure}.
     *
     * @param targetUserId ID dell'utente di cui recuperare i documenti
     * @param type         tipo documento da filtrare
     * @param callerId     ID dell'utente che effettua la richiesta
     * @return lista di DTO dei documenti del tipo richiesto
     * @throws com.project.tesi.exception.common.UnauthorizedAccessException se il chiamante non è autorizzato
     */
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
            throw new UnauthorizedAccessException("Non sei autorizzato a visualizzare questi documenti");
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
            throw new UnauthorizedAccessException("Non sei autorizzato a modificare le note di questo documento");
        }
        return documentMapper.toUpdatedNotesResponse(documentService.updateNotes(id, notes));
    }
}

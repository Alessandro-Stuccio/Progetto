package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.DocumentType;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.exception.document.InvalidFileException;
import com.project.tesi.facade.InsuranceFacade;
import com.project.tesi.mapper.DocumentMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import com.project.tesi.service.DocumentService;
import com.project.tesi.service.FileStorageService;
import com.project.tesi.service.SubscriptionService;
import com.project.tesi.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Implementazione di {@link com.project.tesi.facade.InsuranceFacade}.
 * Gestisce le operazioni dell'Insurance Manager: visibilità dei clienti,
 * abbonamenti attivi e polizze assicurative (documenti di tipo INSURANCE_POLICY).
 */
@Component
public class InsuranceFacadeImpl implements InsuranceFacade {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final UserMapper userMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final DocumentMapper documentMapper;

    public InsuranceFacadeImpl(UserService userService,
                               SubscriptionService subscriptionService,
                               DocumentService documentService,
                               FileStorageService fileStorageService,
                               UserMapper userMapper,
                               SubscriptionMapper subscriptionMapper,
                               DocumentMapper documentMapper) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
        this.userMapper = userMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.documentMapper = documentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllClients() {
        return userMapper.toAdminResponse(userService.findByRole(Role.CLIENT));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getChatContacts() {
        return userService.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.MODERATOR)
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionService.getAllSubscriptions().stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Document getDocumentById(Long documentId) {
        Document doc = documentService.getDocumentById(documentId);
        requireInsurancePolice(doc);
        return doc;
    }

    /**
     * Carica una polizza assicurativa associandola a un cliente.
     * Verifica che il destinatario abbia ruolo {@code CLIENT} e forza il tipo
     * documento a {@link com.project.tesi.enums.DocumentType#INSURANCE_POLICE}.
     * In caso di errore durante la creazione del record DB rimuove il file già salvato.
     *
     * @param file     file della polizza
     * @param clientId ID del cliente a cui associare la polizza
     * @param callerId ID dell'Insurance Manager che esegue l'upload
     * @return DTO con i metadati della polizza caricata
     * @throws com.project.tesi.exception.document.InvalidFileException se il destinatario non è un CLIENT
     */
    @Override
    @Transactional
    public DocumentUploadResponse uploadPolicy(MultipartFile file, Long clientId, Long callerId) {
        User client = userService.getUserById(clientId);
        if (client.getRole() != Role.CLIENT) {
            throw new InvalidFileException("Le polizze assicurative possono essere assegnate solo a clienti.");
        }
        User uploader = userService.getUserById(callerId);
        String filePath = fileStorageService.store(file);
        Document doc;
        try {
            doc = documentService.uploadDocument(
                    filePath, file.getOriginalFilename(), file.getContentType(),
                    DocumentType.INSURANCE_POLICE.name(), client, uploader);
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

    /**
     * Scarica i byte di una polizza assicurativa.
     * Verifica tramite {@link #requireInsurancePolice} che il documento
     * sia effettivamente di tipo {@code INSURANCE_POLICE}.
     *
     * @param documentId ID della polizza da scaricare
     * @return array di byte del file
     * @throws com.project.tesi.exception.common.UnauthorizedAccessException se il documento non è una polizza assicurativa
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] downloadPolicy(Long documentId) {
        Document doc = documentService.getDocumentById(documentId);
        requireInsurancePolice(doc);
        return fileStorageService.load(doc.getFilePath());
    }

    /**
     * Elimina una polizza assicurativa.
     * Verifica tramite {@link #requireInsurancePolice} che il documento sia di tipo
     * {@code INSURANCE_POLICE}, poi rimuove il record DB e il file da filesystem.
     *
     * @param documentId ID della polizza da eliminare
     * @throws com.project.tesi.exception.common.UnauthorizedAccessException se il documento non è una polizza assicurativa
     */
    @Override
    @Transactional
    public void deletePolicy(Long documentId) {
        Document doc = documentService.getDocumentById(documentId);
        requireInsurancePolice(doc);
        String filePath = doc.getFilePath();
        documentService.deleteDocument(documentId);
        fileStorageService.delete(filePath);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getClientPolicies(Long clientId) {
        User client = userService.getUserById(clientId);
        if (client.getRole() != Role.CLIENT) {
            throw new InvalidFileException("Le polizze assicurative possono essere assegnate solo a clienti.");
        }
        return documentMapper.toResponseList(
                documentService.getUserDocumentsByType(client, DocumentType.INSURANCE_POLICE.name()));
    }

    @Override
    @Transactional
    public UpdatedNotesResponse updatePolicyNotes(Long documentId, String notes) {
        Document doc = documentService.getDocumentById(documentId);
        requireInsurancePolice(doc);
        return documentMapper.toUpdatedNotesResponse(documentService.updateNotes(documentId, notes));
    }

    private void requireInsurancePolice(Document doc) {
        if (doc.getType() != DocumentType.INSURANCE_POLICE) {
            throw new UnauthorizedAccessException("Il documento non è una polizza assicurativa.");
        }
    }
}

package com.project.kore.facade.impl;

import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.DocumentUploadResponse;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.enums.DocumentType;
import com.project.kore.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import com.project.kore.exception.document.InvalidFileException;
import com.project.kore.facade.InsuranceFacade;
import com.project.kore.mapper.DocumentMapper;
import com.project.kore.mapper.SubscriptionMapper;
import com.project.kore.mapper.UserMapper;
import com.project.kore.model.Document;
import com.project.kore.model.User;
import com.project.kore.service.DocumentService;
import com.project.kore.service.FileStorageService;
import com.project.kore.service.SubscriptionService;
import com.project.kore.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Operazioni dell'Insurance Manager: clienti, abbonamenti e polizze (documenti di tipo INSURANCE_POLICE).
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
     * Carica una polizza per un cliente, forzando il tipo a INSURANCE_POLICE. Se la scrittura del
     * record fallisce, il file appena salvato viene rimosso per non lasciare orfani sul filesystem.
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

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadPolicy(Long documentId) {
        Document doc = documentService.getDocumentById(documentId);
        requireInsurancePolice(doc);
        return fileStorageService.load(doc.getFilePath());
    }

    // Rimuove sia il record sia il file; requireInsurancePolice impedisce di toccare documenti di altro tipo.
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
            throw new AccessDeniedException("Il documento non è una polizza assicurativa.");
        }
    }
}

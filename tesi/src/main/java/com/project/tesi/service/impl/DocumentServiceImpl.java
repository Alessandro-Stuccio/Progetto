package com.project.tesi.service.impl;

import com.project.tesi.enums.DocumentType;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.document.DocumentNotFoundException;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import com.project.tesi.repository.DocumentRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.DocumentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Document uploadDocument(String filePath, String originalName, String contentType,
                                   String docTypeStr, Long clientId, Long uploaderId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clientId));
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new ResourceNotFoundException("Uploader", uploaderId));

        Document doc = Document.builder()
                .fileName(originalName)
                .filePath(filePath)
                .contentType(contentType)
                .type(DocumentType.valueOf(docTypeStr))
                .owner(client)
                .uploadedBy(uploader)
                .uploadDate(LocalDateTime.now())
                .build();

        return documentRepository.save(doc);
    }

    @Override
    public List<Document> findRecentByOwner(User owner, LocalDateTime since) {
        return documentRepository.findRecentByOwner(owner, since);
    }

    @Override
    public List<Document> findRecentByProfessional(User professional, LocalDateTime since) {
        return documentRepository.findRecentByUploader(professional, since);
    }

    @Override
    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    @Override
    public List<Document> getUserDocuments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userId));
        return documentRepository.findByOwnerOrderByUploadDateDesc(user);
    }

    @Override
    public List<Document> getUserDocumentsByType(Long userId, String docType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userId));
        DocumentType type = DocumentType.valueOf(docType);
        return documentRepository.findByOwnerAndTypeOrderByUploadDateDesc(user, type);
    }

    @Override
    public void deleteDocument(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        documentRepository.delete(doc);
    }

    @Override
    public Document updateNotes(Long documentId, String notes) {
        Document doc = getDocumentById(documentId);
        doc.setNotes(notes);
        return documentRepository.save(doc);
    }

    @Override
    public void deleteByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userId));
        documentRepository.deleteByUserId(userId);
    }

    @Override
    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }
}

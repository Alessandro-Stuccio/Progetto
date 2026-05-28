package com.project.tesi.facade;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentFacade {
    DocumentUploadResponse uploadDocumentWithValidation(MultipartFile file, Long clientId, Long uploaderId, String type);
    Document getDocumentById(Long id);
    void deleteDocument(Long id, Long callerId);
    byte[] downloadDocumentSecure(Long id, Long callerId);
    List<DocumentResponse> getUserDocumentsDtoSecure(Long targetUserId, Long callerId);
    List<DocumentResponse> getUserDocumentsByTypeDtoSecure(Long targetUserId, String type, Long callerId);
    UpdatedNotesResponse updateNotes(Long id, String notes, Long callerId);
}

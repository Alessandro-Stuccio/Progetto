package com.project.tesi.facade;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InsuranceFacade {
    List<UserResponse> getAllClients();
    List<SubscriptionResponse> getAllSubscriptions();
    List<UserResponse> getChatContacts();
    Document getDocumentById(Long documentId);
    DocumentUploadResponse uploadPolicy(MultipartFile file, Long clientId, Long callerId);
    byte[] downloadPolicy(Long documentId);
    void deletePolicy(Long documentId);
    List<DocumentResponse> getClientPolicies(Long clientId);
    UpdatedNotesResponse updatePolicyNotes(Long documentId, String notes);
}

package com.project.tesi.service.impl;

import com.project.tesi.enums.DocumentType;
import com.project.tesi.enums.Role;
import com.project.tesi.model.Document;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.repository.DocumentRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.ProfessionalStatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfessionalStatsServiceImpl implements ProfessionalStatsService {

    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final DocumentRepository documentRepository;

    public ProfessionalStatsServiceImpl(UserRepository userRepository,
                                         SlotRepository slotRepository,
                                         DocumentRepository documentRepository) {
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Slot> getTodaySlots(User professional, LocalDateTime dayStart, LocalDateTime dayEnd) {
        return slotRepository.findTodayByProfessional(professional, dayStart, dayEnd);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAssignedClients(User professional) {
        if (professional.getRole() == Role.PERSONAL_TRAINER) {
            return userRepository.findByAssignedPT(professional);
        }
        return userRepository.findByAssignedNutritionist(professional);
    }

    @Override
    @Transactional(readOnly = true)
    public Document getLatestDocumentByOwnerAndType(User owner, DocumentType type) {
        return documentRepository.findLatestByOwnerAndType(owner, type);
    }

    @Override
    @Transactional(readOnly = true)
    public int countDocumentsUploadedSince(User professional, LocalDateTime since) {
        return documentRepository.countByUploaderSince(professional, since);
    }
}

package com.project.tesi.mapper;

import com.project.tesi.dto.response.ActivityFeedItemResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.model.Document;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActivityFeedMapper {

    public List<ActivityFeedItemResponse> toActivityFeedItemResponse(List<Slot> slots, List<Document> documents, User user) {
        List<ActivityFeedItemResponse> result = new ArrayList<>();
        for (Slot slot : slots) {
            result.add(toActivityFeedItemResponse(slot, user));
        }
        for (Document document : documents) {
            result.add(toActivityFeedItemResponse(document, user));
        }
        result.sort((a, b) -> b.timestamp().compareTo(a.timestamp()));
        return result;
    }

    private ActivityFeedItemResponse toActivityFeedItemResponse(Slot slot, User user) {
        if (user.getRole() == Role.CLIENT) return toActivityFeedItemResponseSlotUser(slot);
        else return toActivityFeedItemResponseSlotProfessional(slot);
    }

    private ActivityFeedItemResponse toActivityFeedItemResponseSlotUser(Slot slot) {
        User professional = slot.getProfessional();
        String proName = professional.getFirstName();
        String proRole = professional.getRole() == Role.PERSONAL_TRAINER ? "PT" : "Nutrizionista";
        return new ActivityFeedItemResponse("Booking", "Appuntamento prenotato con " + proRole + " " + proName, slot.getStartTime());
    }

    private ActivityFeedItemResponse toActivityFeedItemResponseSlotProfessional(Slot slot) {
        String clientName = slot.getBookedBy() != null ? slot.getBookedBy().getFullName() : "";
        return new ActivityFeedItemResponse("Booking", clientName + " ha prenotato un appuntamento", slot.getStartTime());
    }

    private ActivityFeedItemResponse toActivityFeedItemResponse(Document document, User user) {
        if (user.getRole() == Role.CLIENT) return toActivityFeedItemResponseDocumentUser(document);
        else return toActivityFeedItemResponseDocumentProfessional(document);
    }

    private ActivityFeedItemResponse toActivityFeedItemResponseDocumentProfessional(Document document) {
        String clientName = document.getOwner() != null ? document.getOwner().getFullName() : "";
        return new ActivityFeedItemResponse("Document", document.getType().getDesc() + " caricata per " + clientName, document.getUploadDate());
    }

    private ActivityFeedItemResponse toActivityFeedItemResponseDocumentUser(Document document) {
        String uploaderName = document.getUploadedBy() != null ? document.getUploadedBy().getFirstName() : "Sistema";
        return new ActivityFeedItemResponse("Document", document.getType().getDesc() + " caricata da " + uploaderName, document.getUploadDate());
    }
}

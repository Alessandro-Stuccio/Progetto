package com.project.tesi.mapper;

import com.project.tesi.dto.response.ActivityFeedItemResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.model.Booking;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActivityFeedMapper {
    public List<ActivityFeedItemResponse> toActivityFeedItemResponse(List<Booking> sortable, List<Document> documents, User user) {
        List<ActivityFeedItemResponse> activityFeedItemResponses = new ArrayList<>();
        for (Booking booking : sortable) {
            activityFeedItemResponses.add(toActivityFeedItemResponse(booking, user));
        }
        for (Document document : documents) {
            activityFeedItemResponses.add(toActivityFeedItemResponse(document, user));
        }
        activityFeedItemResponses.sort((a, b) -> b.timestamp().compareTo(a.timestamp()));
        return activityFeedItemResponses;
    }

    private ActivityFeedItemResponse toActivityFeedItemResponse(Booking booking, User user) {
        if (user.getRole() == Role.CLIENT) return toActivityFeedItemResponseBookingUser(booking);
        else return toActivityFeedItemResponseBookingProfessional(booking);
    }

    private ActivityFeedItemResponse toActivityFeedItemResponseBookingUser(Booking booking) {
        User professional = booking.getSlot().getProfessional();
        String proName = professional.getFirstName();
        String proRole = professional.getRole() == Role.PERSONAL_TRAINER ? "PT" : "Nutrizionista";
        return new ActivityFeedItemResponse("Booking", "Appuntamento prenotato con " + proRole + " " + proName, booking.getSlot().getStartTime());
    }

    private ActivityFeedItemResponse toActivityFeedItemResponseBookingProfessional(Booking booking) {
        String clientName = booking.getUser().getFullName();
        return new ActivityFeedItemResponse("Booking", clientName + " ha prenotato un appuntamento", booking.getSlot().getStartTime());
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

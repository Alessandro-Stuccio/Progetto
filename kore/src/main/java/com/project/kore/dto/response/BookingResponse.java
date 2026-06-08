package com.project.kore.dto.response;

import com.project.kore.enums.BookingStatus;
import com.project.kore.enums.Role;
import java.util.Objects;

/**
 * Dati di una prenotazione restituiti al client: orario, controparte, link al meeting e stato.
 * canJoin è calcolato: indica se la videochiamata è avviabile nella finestra di ±30 minuti dall'orario.
 */
public class BookingResponse {

    private Long id;
    private String date;
    private String startTime;
    private String endTime;
    private String professionalName;
    private String clientName;
    private Role professionalRole;
    private String meetingLink;
    private BookingStatus status;
    private boolean canJoin;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getProfessionalName() { return professionalName; }
    public void setProfessionalName(String professionalName) { this.professionalName = professionalName; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public Role getProfessionalRole() { return professionalRole; }
    public void setProfessionalRole(Role professionalRole) { this.professionalRole = professionalRole; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public boolean isCanJoin() { return canJoin; }
    public void setCanJoin(boolean canJoin) { this.canJoin = canJoin; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingResponse that = (BookingResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BookingResponse{id=" + id + ", date='" + date + "', status=" + status + ", canJoin=" + canJoin + "}";
    }

    public static class Builder {
        private Long id;
        private String date;
        private String startTime;
        private String endTime;
        private String professionalName;
        private String clientName;
        private Role professionalRole;
        private String meetingLink;
        private BookingStatus status;
        private boolean canJoin;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder date(String date) { this.date = date; return this; }
        public Builder startTime(String startTime) { this.startTime = startTime; return this; }
        public Builder endTime(String endTime) { this.endTime = endTime; return this; }
        public Builder professionalName(String professionalName) { this.professionalName = professionalName; return this; }
        public Builder clientName(String clientName) { this.clientName = clientName; return this; }
        public Builder professionalRole(Role professionalRole) { this.professionalRole = professionalRole; return this; }
        public Builder meetingLink(String meetingLink) { this.meetingLink = meetingLink; return this; }
        public Builder status(BookingStatus status) { this.status = status; return this; }
        public Builder canJoin(boolean canJoin) { this.canJoin = canJoin; return this; }

        public BookingResponse build() {
            BookingResponse response = new BookingResponse();
            response.id = this.id;
            response.date = this.date;
            response.startTime = this.startTime;
            response.endTime = this.endTime;
            response.professionalName = this.professionalName;
            response.clientName = this.clientName;
            response.professionalRole = this.professionalRole;
            response.meetingLink = this.meetingLink;
            response.status = this.status;
            response.canJoin = this.canJoin;
            return response;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}

package com.project.kore.dto.response.stats;

import java.util.List;

/**
 * Metriche per la dashboard del professionista: appuntamenti di oggi, clienti da seguire e
 * qualche contatore di riepilogo.
 */
public class ProfessionalStatsResponse {

    private List<TodayBookingItem> todayBookings;
    private int todayBookingsCount;
    private List<ClientAttentionItem> clientsNeedingAttention;
    private int clientsNeedingAttentionCount;
    private int docsUploadedThisWeek;
    private int totalClients;


    private ProfessionalStatsResponse(Builder b) {
        this.todayBookings = b.todayBookings;
        this.todayBookingsCount = b.todayBookingsCount;
        this.clientsNeedingAttention = b.clientsNeedingAttention;
        this.clientsNeedingAttentionCount = b.clientsNeedingAttentionCount;
        this.docsUploadedThisWeek = b.docsUploadedThisWeek;
        this.totalClients = b.totalClients;
    }

    public static Builder builder() { return new Builder(); }

    public List<TodayBookingItem> getTodayBookings() { return todayBookings; }
    public int getTodayBookingsCount() { return todayBookingsCount; }
    public List<ClientAttentionItem> getClientsNeedingAttention() { return clientsNeedingAttention; }
    public int getClientsNeedingAttentionCount() { return clientsNeedingAttentionCount; }
    public int getDocsUploadedThisWeek() { return docsUploadedThisWeek; }
    public int getTotalClients() { return totalClients; }

    public static class Builder {
        private List<TodayBookingItem> todayBookings;
        private int todayBookingsCount;
        private List<ClientAttentionItem> clientsNeedingAttention;
        private int clientsNeedingAttentionCount;
        private int docsUploadedThisWeek;
        private int totalClients;

        public Builder todayBookings(List<TodayBookingItem> v) { this.todayBookings = v; return this; }
        public Builder todayBookingsCount(int v) { this.todayBookingsCount = v; return this; }
        public Builder clientsNeedingAttention(List<ClientAttentionItem> v) { this.clientsNeedingAttention = v; return this; }
        public Builder clientsNeedingAttentionCount(int v) { this.clientsNeedingAttentionCount = v; return this; }
        public Builder docsUploadedThisWeek(int v) { this.docsUploadedThisWeek = v; return this; }
        public Builder totalClients(int v) { this.totalClients = v; return this; }

        public ProfessionalStatsResponse build() { return new ProfessionalStatsResponse(this); }
    }

    /** Una prenotazione odierna mostrata nell'agenda del professionista. */
    public static class TodayBookingItem {
        private Long id;
        private String clientName;
        private Long clientId;
        private String startTime;
        private String endTime;
        private String status;
        private String meetingLink;


        private TodayBookingItem(Builder b) {
            this.id = b.id;
            this.clientName = b.clientName;
            this.clientId = b.clientId;
            this.startTime = b.startTime;
            this.endTime = b.endTime;
            this.status = b.status;
            this.meetingLink = b.meetingLink;
        }

        public static Builder builder() { return new Builder(); }

        public Long getId() { return id; }
        public String getClientName() { return clientName; }
        public Long getClientId() { return clientId; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getStatus() { return status; }
        public String getMeetingLink() { return meetingLink; }

        public static class Builder {
            private Long id;
            private String clientName;
            private Long clientId;
            private String startTime;
            private String endTime;
            private String status;
            private String meetingLink;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder clientName(String clientName) { this.clientName = clientName; return this; }
            public Builder clientId(Long clientId) { this.clientId = clientId; return this; }
            public Builder startTime(String startTime) { this.startTime = startTime; return this; }
            public Builder endTime(String endTime) { this.endTime = endTime; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder meetingLink(String meetingLink) { this.meetingLink = meetingLink; return this; }

            public TodayBookingItem build() { return new TodayBookingItem(this); }
        }
    }

    /** Un cliente da ricontattare; daysSinceLastDoc è i giorni trascorsi dall'ultimo documento caricato. */
    public static class ClientAttentionItem {
        private Long id;
        private String firstName;
        private String lastName;
        private String lastDocDate;
        private long daysSinceLastDoc;


        private ClientAttentionItem(Builder b) {
            this.id = b.id;
            this.firstName = b.firstName;
            this.lastName = b.lastName;
            this.lastDocDate = b.lastDocDate;
            this.daysSinceLastDoc = b.daysSinceLastDoc;
        }

        public static Builder builder() { return new Builder(); }

        public Long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getLastDocDate() { return lastDocDate; }
        public long getDaysSinceLastDoc() { return daysSinceLastDoc; }

        public static class Builder {
            private Long id;
            private String firstName;
            private String lastName;
            private String lastDocDate;
            private long daysSinceLastDoc;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder firstName(String firstName) { this.firstName = firstName; return this; }
            public Builder lastName(String lastName) { this.lastName = lastName; return this; }
            public Builder lastDocDate(String lastDocDate) { this.lastDocDate = lastDocDate; return this; }
            public Builder daysSinceLastDoc(long daysSinceLastDoc) { this.daysSinceLastDoc = daysSinceLastDoc; return this; }

            public ClientAttentionItem build() { return new ClientAttentionItem(this); }
        }
    }
}

package com.project.tesi.service;

import org.springframework.validation.annotation.Validated;

import com.project.tesi.dto.request.JobApplicationRequest;
import org.springframework.web.multipart.MultipartFile;

/** Invio email tramite l'API di Resend. */
@Validated
public interface EmailService {

    /** Inoltra all'admin una candidatura di lavoro con il CV in allegato. */
    void sendJobApplication(JobApplicationRequest request, MultipartFile cv);

    /** Parte asincrona dell'invio candidatura: il file è già stato letto in memoria. */
    void sendEmailAsync(JobApplicationRequest request, byte[] cvBytes, String cvFileName, String cvContentType);

    /** Email di benvenuto al nuovo utente registrato. */
    void sendWelcomeEmail(String toEmail, String firstName);

    /** Promemoria per una prenotazione imminente. Il testo cambia se il destinatario è il cliente. */
    void sendBookingReminderEmail(String toEmail, String recipientName, String otherPartyName,
                                   java.time.LocalDateTime startTime, String meetingLink, boolean isForClient);

    /** Email con il link per reimpostare la password (resetToken da inserire nel link). */
    void sendPasswordResetEmail(String toEmail, String firstName, String resetToken);

    /** Notifica che la password è stata cambiata. */
    void sendPasswordChangeEmail(String toEmail, String firstName);

    /** Conferma di una prenotazione appena creata, con il link Jitsi. */
    void sendBookingConfirmationEmail(String toEmail, String recipientName, String otherPartyName,
                                   java.time.LocalDateTime startTime, String meetingLink);

    /** Notifica l'annullamento di una prenotazione. */
    void sendBookingCancellationEmail(String toEmail, String recipientName, String otherPartyName,
                                   java.time.LocalDateTime startTime);
}

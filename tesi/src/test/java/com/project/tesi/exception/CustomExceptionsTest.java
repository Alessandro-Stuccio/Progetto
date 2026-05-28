package com.project.tesi.exception;

import com.project.tesi.exception.booking.*;
import com.project.tesi.exception.chat.ChatNotAllowedException;
import com.project.tesi.exception.common.*;
import com.project.tesi.exception.document.*;
import com.project.tesi.exception.review.ReviewNotAllowedException;
import com.project.tesi.exception.subscription.SubscriptionNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test unitari per tutte le eccezioni personalizzate.
 */
class CustomExceptionsTest {


    @Test
    @DisplayName("ResourceNotFoundException — con messaggio")
    void resourceNotFoundException_message() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Risorsa non trovata");
        assertThat(ex.getMessage()).isEqualTo("Risorsa non trovata");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("ResourceNotFoundException — con nome e ID")
    void resourceNotFoundException_nameId() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Utente", 42L);
        assertThat(ex.getMessage()).contains("42");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("ResourceNotFoundException — con nome, campo e valore")
    void resourceNotFoundException_nameFieldValue() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Utente", "email", "test@test.com");
        assertThat(ex.getMessage()).contains("email").contains("test@test.com");
    }

    @Test
    @DisplayName("ResourceAlreadyExistsException — con messaggio e con nome, campo, valore")
    void resourceAlreadyExistsException() {
        ResourceAlreadyExistsException ex1 = new ResourceAlreadyExistsException("Duplicato");
        assertThat(ex1.getStatus()).isEqualTo(HttpStatus.CONFLICT);

        ResourceAlreadyExistsException ex2 = new ResourceAlreadyExistsException("Utente", "email", "test@test.com");
        assertThat(ex2.getMessage()).contains("email");
    }

    @Test
    @DisplayName("BusinessLogicException — 422")
    void businessLogicException() {
        BusinessLogicException ex = new BusinessLogicException("Regola violata");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.valueOf(422));
    }

    @Test
    @DisplayName("UnauthorizedAccessException — 403")
    void unauthorizedAccessException() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Accesso negato");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    @DisplayName("InsufficientCreditsException — 422")
    void insufficientCreditsException() {
        InsufficientCreditsException ex = new InsufficientCreditsException("PT");
        assertThat(ex.getMessage()).contains("PT");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.valueOf(422));
    }

    @Test
    @DisplayName("ProfessionalNotAssignedException — costruttori")
    void professionalNotAssignedException() {
        ProfessionalNotAssignedException ex1 = new ProfessionalNotAssignedException("PT");
        assertThat(ex1.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        ProfessionalNotAssignedException ex2 = new ProfessionalNotAssignedException("PT", "Messaggio custom");
        assertThat(ex2.getMessage()).isEqualTo("Messaggio custom");
    }

    @Test
    @DisplayName("ProfessionalSoldOutException — 422")
    void professionalSoldOutException() {
        ProfessionalSoldOutException ex = new ProfessionalSoldOutException("Luca");
        assertThat(ex.getMessage()).contains("Luca");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.valueOf(422));
    }

    @Test
    @DisplayName("SlotAlreadyBookedException — costruttori")
    void slotAlreadyBookedException() {
        SlotAlreadyBookedException ex1 = new SlotAlreadyBookedException();
        assertThat(ex1.getStatus()).isEqualTo(HttpStatus.CONFLICT);

        SlotAlreadyBookedException ex2 = new SlotAlreadyBookedException("Custom");
        assertThat(ex2.getMessage()).isEqualTo("Custom");
    }


    @Test
    @DisplayName("ChatNotAllowedException — costruttori")
    void chatNotAllowedException() {
        ChatNotAllowedException ex1 = new ChatNotAllowedException();
        assertThat(ex1.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        ChatNotAllowedException ex2 = new ChatNotAllowedException("Custom");
        assertThat(ex2.getMessage()).isEqualTo("Custom");
    }


    @Test
    @DisplayName("DocumentNotFoundException — costruttori")
    void documentNotFoundException() {
        DocumentNotFoundException ex1 = new DocumentNotFoundException(42L);
        assertThat(ex1.getMessage()).contains("42");
        assertThat(ex1.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        DocumentNotFoundException ex2 = new DocumentNotFoundException("Custom");
        assertThat(ex2.getMessage()).isEqualTo("Custom");
    }

    @Test
    @DisplayName("DocumentStorageException — costruttori")
    void documentStorageException() {
        DocumentStorageException ex1 = new DocumentStorageException("Errore I/O");
        assertThat(ex1.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        RuntimeException cause = new RuntimeException("Causa originale");
        DocumentStorageException ex2 = new DocumentStorageException("Errore I/O", cause);
        assertThat(ex2.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("InvalidFileException — 400")
    void invalidFileException() {
        InvalidFileException ex = new InvalidFileException("File non valido");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    @DisplayName("ReviewNotAllowedException — 422")
    void reviewNotAllowedException() {
        ReviewNotAllowedException ex = new ReviewNotAllowedException("Troppo presto");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.valueOf(422));
    }


    @Test
    @DisplayName("SubscriptionNotFoundException — costruttori")
    void subscriptionNotFoundException() {
        SubscriptionNotFoundException ex1 = new SubscriptionNotFoundException();
        assertThat(ex1.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        SubscriptionNotFoundException ex2 = new SubscriptionNotFoundException("Custom");
        assertThat(ex2.getMessage()).isEqualTo("Custom");
    }
}


package com.project.tesi.controller;

import com.project.tesi.dto.request.ForgotPasswordRequest;
import com.project.tesi.dto.request.LoginRequest;
import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.request.ResetPasswordRequest;
import com.project.tesi.dto.response.AuthResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.facade.AuthFacade;
import com.project.tesi.facade.UserFacade;
import com.project.tesi.model.User;
import com.project.tesi.service.AuthResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registrazione, login, recupero e reset password")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthFacade authFacade;
    private final UserFacade userFacade;

    public AuthController(AuthFacade authFacade, UserFacade userFacade) {
        this.authFacade = authFacade;
        this.userFacade = userFacade;
    }

    @Operation(summary = "Registra un nuovo utente", description = "Crea un account CLIENT e restituisce il profilo appena creato.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utente registrato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati di registrazione non validi"),
        @ApiResponse(responseCode = "409", description = "Email già in uso")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registrazione nuovo utente: {}", request.email());
        UserResponse response = userFacade.registerUser(request);
        log.info("Utente registrato con successo: id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Login", description = "Autentica email e password e restituisce il token JWT.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login effettuato con successo"),
        @ApiResponse(responseCode = "401", description = "Credenziali non valide")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Tentativo di login: {}", request.email());
        AuthResult result = authFacade.login(request);
        User u = result.user();
        return ResponseEntity.ok(AuthResponse.builder()
                .token(result.token())
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .role(u.getRole())
                .profilePicture(u.getProfilePicture())
                .build());
    }

    @Operation(summary = "Richiesta reset password", description = "Invia un link di reset all'email indicata (valido 30 minuti).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Email inviata se l'account esiste"),
        @ApiResponse(responseCode = "400", description = "Email non valida")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authFacade.forgotPassword(request.email());
        return ResponseEntity.ok(Map.of("message", "Link di reset inviato. Controlla la tua casella di posta."));
    }

    @Operation(summary = "Reset password", description = "Reimposta la password usando il token ricevuto via email.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password reimpostata con successo"),
        @ApiResponse(responseCode = "400", description = "Token non valido o scaduto")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authFacade.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password reimpostata con successo."));
    }

    @Operation(summary = "Health check", description = "Verifica che il backend sia raggiungibile e operativo.")
    @ApiResponse(responseCode = "200", description = "Backend online")
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "UP", "message", "Il Backend è online e funziona correttamente"));
    }
}

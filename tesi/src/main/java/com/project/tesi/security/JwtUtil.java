package com.project.tesi.security;

import com.project.tesi.service.RandomGenerationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility per la generazione e validazione dei token JWT. Supporta due tipi
 * di token: auth token (scadenza configurabile, default 24 h) e password-reset
 * token (scadenza fissa 30 minuti). Il claim {@code purpose} distingue i due tipi.
 */
@Component
public class JwtUtil {



    private static final String PURPOSE_CLAIM = "purpose";
    private static final String PURPOSE_PASSWORD_RESET = "PASSWORD_RESET";
    private static final long PASSWORD_RESET_EXPIRATION_MS = 30 * 60 * 1000L;

    public JwtUtil(RandomGenerationService random) {
        SECRET_KEY =random.getTokenKey();
        LogManager.getLogger(this.getClass()).warn("SECRET_KEY => " + SECRET_KEY);
    }

    private final String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Validata a {@code @PostConstruct}: lancia {@link IllegalStateException}
     * se {@code JWT_SECRET} è mancante o vuota.
     */
    @PostConstruct
    public void validateSecret() {
        if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET non configurata. " +
                "Imposta la variabile d'ambiente JWT_SECRET prima di avviare l'app."
            );
        }
    }

    /**
     * Estrae il subject (email) dal token JWT.
     *
     * @param token il token JWT
     * @return l'email dell'utente
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un JWT di autenticazione per l'utente con scadenza configurata
     * da {@code jwt.expiration}.
     *
     * @param userDetails l'utente autenticato
     * @return il token JWT firmato
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Genera un JWT con claim {@code purpose=PASSWORD_RESET} e scadenza fissa
     * di 30 minuti, da inviare via email per il reset della password.
     *
     * @param email l'indirizzo email del richiedente
     * @return il token di reset firmato
     */
    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .setClaims(Map.of(PURPOSE_CLAIM, PURPOSE_PASSWORD_RESET))
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + PASSWORD_RESET_EXPIRATION_MS))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Verifica il claim {@code purpose} e restituisce l'email del proprietario.
     * Lancia {@link IllegalArgumentException} se il token non è di tipo
     * {@code PASSWORD_RESET}.
     *
     * @param token il token di reset da validare
     * @return l'email estratta dal token
     */
    public String validatePasswordResetToken(String token) {
        Claims claims = extractAllClaims(token);
        String purpose = claims.get(PURPOSE_CLAIM, String.class);
        if (!PURPOSE_PASSWORD_RESET.equals(purpose)) {
            throw new IllegalArgumentException("Token non valido per il reset della password.");
        }
        return claims.getSubject();
    }

    /**
     * Verifica che il token appartenga all'utente indicato e non sia scaduto.
     *
     * @param token       il token JWT
     * @param userDetails l'utente da confrontare
     * @return {@code true} se il token è valido e non scaduto
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

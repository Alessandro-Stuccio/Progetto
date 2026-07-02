package com.project.kore.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Impedisce lo spin-down del free tier di Render: il servizio viene sospeso dopo
 * 15 minuti senza traffico in ingresso, quindi ogni 10 minuti facciamo una GET
 * sul nostro stesso URL pubblico ({@code keepalive.url}, su Render arriva da
 * RENDER_EXTERNAL_URL). Con la proprietà vuota — com'è in dev — non fa nulla.
 */
@Component
public class KeepAliveScheduler {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private final String keepAliveUrl;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public KeepAliveScheduler(@Value("${keepalive.url:}") String keepAliveUrl) {
        String url = keepAliveUrl == null ? "" : keepAliveUrl.trim();
        // Normalizza l'eventuale slash finale per comporre il path dell'health check.
        this.keepAliveUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    // 10 minuti: sotto la soglia di inattività di Render (15), senza sprecare richieste.
    @Scheduled(fixedDelayString = "${keepalive.interval-ms:600000}",
               initialDelayString = "${keepalive.interval-ms:600000}")
    public void ping() {
        if (keepAliveUrl.isBlank()) {
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(keepAliveUrl + "/actuator/health"))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            log.debug("Keep-alive: {} -> HTTP {}", keepAliveUrl, response.statusCode());
        } catch (Exception e) {
            // Non deve mai disturbare l'applicazione: al prossimo giro riprova.
            log.warn("Keep-alive fallito verso {}: {}", keepAliveUrl, e.getMessage());
        }
    }
}

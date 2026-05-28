package com.project.tesi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Inizializzatore del database di log Log4j2. Attivo solo nel profilo {@code dev}:
 * crea il database {@code tesi_logs} e la tabella {@code app_logs} se non esistono,
 * usando JDBC diretto prima che Spring sia completamente avviato.
 */
@Component
@Profile("dev")
public class LogsDatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LogsDatabaseInitializer.class);

    private static final String BASE_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String LOGS_URL = "jdbc:postgresql://localhost:5432/tesi_logs";
    private static final String USER = "postgres";
    private static final String PASS = "secret";

    /**
     * Verifica l'esistenza del database {@code tesi_logs} e lo crea se assente,
     * quindi crea la tabella {@code app_logs} con {@code IF NOT EXISTS}.
     *
     * @param args argomenti di avvio (non utilizzati)
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASS)) {
                conn.setAutoCommit(true);
                boolean exists = false;
                try (ResultSet rs = conn.getMetaData().getCatalogs()) {
                    while (rs.next()) {
                        if ("tesi_logs".equals(rs.getString(1))) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("CREATE DATABASE tesi_logs");
                        log.info("Database tesi_logs creato.");
                    }
                }
            }

            try (Connection conn = DriverManager.getConnection(LOGS_URL, USER, PASS);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS app_logs (
                        id         BIGSERIAL    PRIMARY KEY,
                        event_date TIMESTAMPTZ  NOT NULL,
                        level      VARCHAR(10)  NOT NULL,
                        logger     VARCHAR(200),
                        message    TEXT,
                        thread     VARCHAR(100),
                        throwable  TEXT
                    )""");
                log.info("Tabella app_logs pronta.");
            }
        } catch (Exception e) {
            log.warn("Impossibile inizializzare logs database: {}", e.getMessage());
        }
    }
}

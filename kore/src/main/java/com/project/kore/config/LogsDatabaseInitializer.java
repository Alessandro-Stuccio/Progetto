package com.project.kore.config;

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
 * Prepara il database dei log di Log4j2. Solo in profilo dev: crea {@code kore_logs}
 * e la tabella {@code app_logs} se mancano, via JDBC diretto perché serve prima che
 * Spring abbia finito di avviarsi.
 */
@Component
@Profile("dev")
public class LogsDatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LogsDatabaseInitializer.class);

    private static final String BASE_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String LOGS_URL = "jdbc:postgresql://localhost:5432/kore_logs";
    private static final String USER = "postgres";
    private static final String PASS = "secret";

    // Crea il database kore_logs se non c'è, poi la tabella app_logs.
    @Override
    public void run(ApplicationArguments args) {
        try {
            try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASS)) {
                conn.setAutoCommit(true);
                boolean exists = false;
                try (ResultSet rs = conn.getMetaData().getCatalogs()) {
                    while (rs.next()) {
                        if ("kore_logs".equals(rs.getString(1))) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("CREATE DATABASE kore_logs");
                        log.info("Database kore_logs creato.");
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

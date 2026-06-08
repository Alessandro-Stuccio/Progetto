package com.project.kore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Crea i file PDF placeholder per i documenti inseriti da data.sql.
 * Attivo solo con il profilo dev — non tocca ambienti di produzione.
 */
@Component
@Profile("dev")
public class DevFileSeedInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(DevFileSeedInitializer.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> SEED_FILES = List.of(
        "polizza_luca.pdf",
        "scheda_luca_pt1.pdf",
        "dieta_luca_n1.pdf",
        "polizza_sofia.pdf",
        "scheda_sofia_pt1.pdf",
        "dieta_sofia_n2.pdf",
        "polizza_matteo.pdf",
        "scheda_matteo_pt2.pdf",
        "dieta_matteo_n1.pdf",
        "polizza_chiara.pdf",
        "scheda_chiara_pt2.pdf",
        "dieta_chiara_n2.pdf",
        "polizza_elena.pdf",
        "scheda_davide_pt2.pdf"
    );

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Path seedDir = Paths.get(uploadDir, "seed");
        try {
            Files.createDirectories(seedDir);
            byte[] pdfBytes = buildPlaceholderPdf();
            for (String fileName : SEED_FILES) {
                Path filePath = seedDir.resolve(fileName);
                if (!Files.exists(filePath)) {
                    Files.write(filePath, pdfBytes);
                    log.info("[DevSeed] Creato file placeholder: {}", filePath);
                }
            }
        } catch (IOException e) {
            log.warn("[DevSeed] Impossibile creare i file PDF placeholder in {}/seed: {}", uploadDir, e.getMessage());
        }
    }

    private byte[] buildPlaceholderPdf() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write("%PDF-1.4\n".getBytes());

        int o1 = out.size();
        out.write("1 0 obj\n<</Type /Catalog /Pages 2 0 R>>\nendobj\n".getBytes());

        int o2 = out.size();
        out.write("2 0 obj\n<</Type /Pages /Kids [3 0 R] /Count 1>>\nendobj\n".getBytes());

        int o3 = out.size();
        out.write("3 0 obj\n<</Type /Page /Parent 2 0 R /MediaBox [0 0 595 842]>>\nendobj\n".getBytes());

        int xrefPos = out.size();
        String xref = "xref\n0 4\n" +
            "0000000000 65535 f \n" +
            String.format("%010d 00000 n \n", o1) +
            String.format("%010d 00000 n \n", o2) +
            String.format("%010d 00000 n \n", o3) +
            "trailer\n<</Size 4 /Root 1 0 R>>\nstartxref\n" + xrefPos + "\n%%EOF\n";
        out.write(xref.getBytes());

        return out.toByteArray();
    }
}

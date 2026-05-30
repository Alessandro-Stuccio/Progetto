package com.project.tesi;

import com.project.tesi.service.RandomGenerationService;
import org.apache.juli.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.naming.Context;

/**
 * Entry point dell'applicazione Spring Boot. Forza IPv4 per evitare timeout SMTP
 * su IPv6. In assenza di {@code JWT_SECRET} nell'ambiente imposta un valore di
 * default per semplificare il run in IDE.
 */
@SpringBootApplication
@EnableScheduling
public class TesiApplication {

    public static void main(String[] args) {
        // Forza l'utilizzo di IPv4 per risolvere problemi di timeout (es. verso
        // smtp.gmail.com su IPv6)
        System.setProperty("java.net.preferIPv4Stack", "true");

        // Inserimento automatico della variabile JWT_SECRET qualora non venga definita
        // manualmente dall'ambiente
        // per facilitare il run tramite VS Code/IDE.


        SpringApplication.run(TesiApplication.class, args);
    }
}

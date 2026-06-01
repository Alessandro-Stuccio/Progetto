package com.project.tesi;

import com.project.tesi.service.RandomGenerationService;
import org.apache.juli.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.naming.Context;

/**
 * Entry point dell'applicazione Spring Boot. All'avvio forza IPv4 per evitare i
 * timeout SMTP che capitano su IPv6.
 */
@SpringBootApplication
@EnableScheduling
public class TesiApplication {

    public static void main(String[] args) {
        // IPv4 obbligatorio: su IPv6 le connessioni a smtp.gmail.com vanno in timeout.
        System.setProperty("java.net.preferIPv4Stack", "true");

        SpringApplication.run(TesiApplication.class, args);
    }
}

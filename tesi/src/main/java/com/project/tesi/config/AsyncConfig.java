package com.project.tesi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configurazione del thread pool asincrono per operazioni non bloccanti
 * (invio email, salvataggio messaggi). Espone il bean {@code emailTaskExecutor}
 * usato da {@code @Async}.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Crea un {@link ThreadPoolTaskExecutor} con core=2, max=10, coda=100.
     * La policy {@code CallerRunsPolicy} garantisce che, se la coda è piena,
     * il chiamante esegua il task direttamente, evitando la perdita silenziosa
     * di email o messaggi.
     *
     * @return l'executor configurato
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-async-");
        // CallerRunsPolicy: se la coda è piena, il chiamante esegue il task
        // direttamente. Evita di perdere email silenziosamente.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

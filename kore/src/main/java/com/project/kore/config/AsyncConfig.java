package com.project.kore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Thread pool per le operazioni non bloccanti (invio email, salvataggio messaggi).
 * Espone il bean usato dai metodi {@code @Async}.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    // Pool con core=2, max=10 e coda da 100. Se la coda si riempie usiamo
    // CallerRunsPolicy così il chiamante esegue il task di persona invece di
    // perderlo silenziosamente.
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

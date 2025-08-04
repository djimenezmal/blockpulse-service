package com.blockchain.blockpulseservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;

@Configuration
public class ThreadConfig {
    @Bean
    public ThreadFactory analyzerThreadFactory() {
        return r -> {
            Thread thread = new Thread(r, "analyzer-thread");
            thread.setDaemon(true);
            return thread;
        };
    }
}
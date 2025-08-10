package com.blockchain.blockpulseservice.config.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
public class EmitterConfig {

    @Bean
    public CopyOnWriteArrayList<SseEmitter> sseEmitters() {
        return new CopyOnWriteArrayList<>();
    }
}

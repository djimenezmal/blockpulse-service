package com.blockchain.blockpulseservice;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketClientStarter {
    
    private final BTCWebSocketClient btcClient;
    private final KaspaWebSocketClient kaspaClient;
    
    @EventListener(ApplicationReadyEvent.class)
    public void startWebSocketConnections() {
        log.info("Starting WebSocket connections...");
        
        try {
            // Start BTC WebSocket connection
            btcClient.connect();
            log.info("BTC WebSocket client connected");
        } catch (Exception e) {
            log.error("Failed to connect BTC WebSocket client", e);
        }
        
        try {
            // Start Kaspa WebSocket connection
            kaspaClient.connect();
            log.info("Kaspa WebSocket client connected");
        } catch (Exception e) {
            log.error("Failed to connect Kaspa WebSocket client", e);
        }
    }
    
    @PreDestroy
    public void shutdownWebSocketConnections() {
        log.info("Shutting down WebSocket connections...");
        
        try {
            btcClient.shutdown();
        } catch (Exception e) {
            log.error("Error shutting down BTC client", e);
        }
        
        try {
            kaspaClient.shutdown();
        } catch (Exception e) {
            log.error("Error shutting down Kaspa client", e);
        }
    }
}
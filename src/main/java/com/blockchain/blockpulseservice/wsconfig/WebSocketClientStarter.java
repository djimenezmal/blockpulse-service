package com.blockchain.blockpulseservice.wsconfig;

import com.blockchain.blockpulseservice.wsclient.BlockchainInfoWebSocketClient;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketClientStarter {
    
    private final BlockchainInfoWebSocketClient btcClient;

    @EventListener(ApplicationReadyEvent.class)
    public void startWebSocketConnections() {
        log.info("Starting WebSocket connections...");
        
        try {
            btcClient.connect();
            log.info("BTC WebSocket client connected");
        } catch (Exception e) {
            log.error("Failed to connect BTC WebSocket client", e);
        }
    }
    
    @PreDestroy
    public void shutdownWebSocketConnections() {
        log.info("Shutting down WebSocket connections...");
        
        try {
            btcClient.disconnect();
        } catch (Exception e) {
            log.error("Error shutting down BTC client", e);
        }
    }
}
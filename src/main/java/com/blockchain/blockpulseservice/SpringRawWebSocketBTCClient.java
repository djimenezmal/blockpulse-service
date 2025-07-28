package com.blockchain.blockpulseservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringRawWebSocketBTCClient {
    
    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    private WebSocketClient client;
    private WebSocketSession session;
    private volatile boolean shouldReconnect = true;
    
    @PostConstruct
    public void initialize() {
        client = new StandardWebSocketClient();
        connect();
    }
    
    private void connect() {
        try {
            WebSocketHandler handler = new BTCWebSocketHandler();
            
            CompletableFuture<WebSocketSession> future = client.execute(
                handler,
                null,
                URI.create("wss://ws.blockchain.info/inv")
            );
            
            session = future.get();
            log.info("Connected to BTC WebSocket");
            
            // Send subscription messages
            sendSubscriptions();
            
        } catch (Exception e) {
            log.error("Failed to connect to BTC WebSocket", e);
            scheduleReconnect();
        }
    }
    
    private void sendSubscriptions() {
        try {
            if (session != null && session.isOpen()) {
                // Subscribe to unconfirmed transactions
                session.sendMessage(new TextMessage("{\"op\":\"unconfirmed_sub\"}"));
                // Subscribe to blocks
                session.sendMessage(new TextMessage("{\"op\":\"blocks_sub\"}"));
                log.info("Sent BTC subscription messages");
            }
        } catch (Exception e) {
            log.error("Error sending subscription messages", e);
        }
    }
    
    private void scheduleReconnect() {
        if (shouldReconnect) {
            scheduler.schedule(() -> {
                if (shouldReconnect) {
                    log.info("Attempting to reconnect to BTC WebSocket...");
                    connect();
                }
            }, 5, TimeUnit.SECONDS);
        }
    }
    
    @PreDestroy
    public void disconnect() {
        shouldReconnect = false;
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                log.error("Error closing WebSocket session", e);
            }
        }
        scheduler.shutdown();
    }
    
    private class BTCWebSocketHandler implements WebSocketHandler {
        
        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            log.info("WebSocket connection established");
        }
        
        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
            try {
                String payload = message.getPayload().toString();
                processMessage(payload);
            } catch (Exception e) {
                log.error("Error handling WebSocket message", e);
            }
        }
        
        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            log.error("WebSocket transport error", exception);
            scheduleReconnect();
        }
        
        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
            log.warn("WebSocket connection closed: {}", closeStatus);
            scheduleReconnect();
        }
        
        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    }
    
    private void processMessage(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String op = node.has("op") ? node.get("op").asText() : "";
            
            switch (op) {
                case "utx" -> processNewTransaction(node.get("x"));
                case "block" -> processNewBlock(node.get("x"));
                default -> log.debug("Unknown operation: {}", op);
            }
            
        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", message, e);
        }
    }
    
    private void processNewTransaction(JsonNode txNode) {
        // Implementation similar to your existing logic
        log.debug("Processing BTC transaction: {}", txNode);
        // Call transactionService.processNewTransaction(...)
    }
    
    private void processNewBlock(JsonNode blockNode) {
        // Implementation similar to your existing logic  
        log.debug("Processing BTC block: {}", blockNode);
        // Call transactionService.updateConfirmedTransactions(...)
    }
}
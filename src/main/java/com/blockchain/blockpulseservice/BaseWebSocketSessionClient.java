package com.blockchain.blockpulseservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class BaseWebSocketSessionClient implements WebSocketHandler {
    
    protected final WebSocketClient webSocketClient;
    protected final ObjectMapper objectMapper;
    protected final ScheduledExecutorService scheduler;
    protected final URI serverUri;
    
    @Getter
    protected WebSocketSession session;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean shouldReconnect = new AtomicBoolean(true);
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private ScheduledFuture<?> reconnectTask;
    private ScheduledFuture<?> heartbeatTask;
    
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final int INITIAL_RECONNECT_DELAY = 5;
    private static final int MAX_RECONNECT_DELAY = 300; // 5 minutes
    private static final int HEARTBEAT_INTERVAL = 30; // 30 seconds
    
    public BaseWebSocketSessionClient(WebSocketClient webSocketClient, 
                                    ObjectMapper objectMapper,
                                    ScheduledExecutorService scheduler,
                                    URI serverUri) {
        this.webSocketClient = webSocketClient;
        this.objectMapper = objectMapper;
        this.scheduler = scheduler;
        this.serverUri = serverUri;
    }
    
    public void connect() {
        if (connected.get()) {
            log.debug("Already connected to {}", serverUri);
            return;
        }
        
        try {
            log.info("Connecting to WebSocket: {}", serverUri);
            webSocketClient.execute(this, null, serverUri).get();
        } catch (Exception e) {
            log.error("Failed to connect to {}", serverUri, e);
            scheduleReconnect();
        }
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        this.session = session;
        connected.set(true);
        reconnectAttempts.set(0);
        
        log.info("WebSocket connected to: {}", serverUri);
        
        // Cancel any pending reconnect task
        if (reconnectTask != null && !reconnectTask.isDone()) {
            reconnectTask.cancel(false);
        }
        
        // Start heartbeat
        startHeartbeat();
        
        // Perform connection-specific initialization
        try {
            onConnectionEstablished(session);
        } catch (Exception e) {
            log.error("Error in connection established callback", e);
        }
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            if (message instanceof TextMessage) {
                String payload = ((TextMessage) message).getPayload();
                log.debug("Received message from {}: {}", serverUri, payload.substring(0, Math.min(100, payload.length())));
                processMessage(payload);
            } else if (message instanceof BinaryMessage) {
                handleBinaryMessage(session, (BinaryMessage) message);
            } else if (message instanceof PongMessage) {
                log.debug("Received pong from {}", serverUri);
            }
        } catch (Exception e) {
            log.error("Error processing message from {}", serverUri, e);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error for {}", serverUri, exception);
        handleConnectionLoss();
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.warn("WebSocket connection closed for {}: {} - {}", serverUri, closeStatus.getCode(), closeStatus.getReason());
        handleConnectionLoss();
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    private void handleConnectionLoss() {
        connected.set(false);
        
        // Stop heartbeat
        if (heartbeatTask != null && !heartbeatTask.isDone()) {
            heartbeatTask.cancel(false);
        }
        
        // Perform cleanup
        try {
            onConnectionLost();
        } catch (Exception e) {
            log.error("Error in connection lost callback", e);
        }
        
        // Schedule reconnect if enabled
        if (shouldReconnect.get()) {
            scheduleReconnect();
        }
    }
    
    private void scheduleReconnect() {
        int attempts = reconnectAttempts.incrementAndGet();
        
        if (attempts > MAX_RECONNECT_ATTEMPTS) {
            log.error("Max reconnect attempts ({}) reached for {}. Giving up.", MAX_RECONNECT_ATTEMPTS, serverUri);
            return;
        }
        
        // Exponential backoff with jitter
        int delay = Math.min(INITIAL_RECONNECT_DELAY * (int) Math.pow(2, attempts - 1), MAX_RECONNECT_DELAY);
        delay += (int) (Math.random() * 5); // Add jitter
        
        log.info("Scheduling reconnect attempt {} for {} in {} seconds", attempts, serverUri, delay);
        
        reconnectTask = scheduler.schedule(() -> {
            if (shouldReconnect.get() && !connected.get()) {
                connect();
            }
        }, delay, TimeUnit.SECONDS);
    }
    
    private void startHeartbeat() {
        heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            if (connected.get() && session != null && session.isOpen()) {
                try {
                    session.sendMessage(new PingMessage());
                    log.debug("Sent ping to {}", serverUri);
                } catch (IOException e) {
                    log.error("Failed to send heartbeat ping to {}", serverUri, e);
                    handleConnectionLoss();
                }
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }
    
    protected void sendMessage(String message) {
        if (!connected.get() || session == null || !session.isOpen()) {
            log.warn("Cannot send message - not connected to {}", serverUri);
            return;
        }
        
        try {
            session.sendMessage(new TextMessage(message));
            log.debug("Sent message to {}: {}", serverUri, message);
        } catch (IOException e) {
            log.error("Failed to send message to {}: {}", serverUri, message, e);
            handleConnectionLoss();
        }
    }
    
    protected void sendBinaryMessage(byte[] data) {
        if (!connected.get() || session == null || !session.isOpen()) {
            log.warn("Cannot send binary message - not connected to {}", serverUri);
            return;
        }
        
        try {
            session.sendMessage(new BinaryMessage(data));
            log.debug("Sent binary message to {} ({} bytes)", serverUri, data.length);
        } catch (IOException e) {
            log.error("Failed to send binary message to {}", serverUri, e);
            handleConnectionLoss();
        }
    }
    
    public void disconnect() {
        shouldReconnect.set(false);
        
        // Cancel scheduled tasks
        if (reconnectTask != null && !reconnectTask.isDone()) {
            reconnectTask.cancel(false);
        }
        if (heartbeatTask != null && !heartbeatTask.isDone()) {
            heartbeatTask.cancel(false);
        }
        
        // Close session
        if (session != null && session.isOpen()) {
            try {
                session.close(CloseStatus.NORMAL);
            } catch (IOException e) {
                log.error("Error closing WebSocket session for {}", serverUri, e);
            }
        }
        
        connected.set(false);
        log.info("Disconnected from {}", serverUri);
    }
    
    public boolean isConnected() {
        return connected.get() && session != null && session.isOpen();
    }

    // Abstract methods to be implemented by subclasses
    protected abstract void onConnectionEstablished(WebSocketSession session);
    protected abstract void processMessage(String message) throws Exception;
    protected abstract void onConnectionLost();
    
    // Optional method for handling binary messages
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        log.debug("Received binary message from {} ({} bytes)", serverUri, message.getPayloadLength());
    }
}
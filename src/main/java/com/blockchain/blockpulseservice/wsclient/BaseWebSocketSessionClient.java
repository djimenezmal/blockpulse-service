package com.blockchain.blockpulseservice.wsclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public abstract class BaseWebSocketSessionClient implements WebSocketHandler {
    private static final int MAX_BUFFER_SIZE = 4 * 1024 * 1024;

    protected final WebSocketClient webSocketClient;
    protected final ObjectMapper objectMapper;
    protected final URI serverUri;

    @Getter
    protected WebSocketSession session;

    private final ConnectionStateManager connectionState;
    private final HeartbeatManager heartbeatManager;
    private final ReconnectionManager reconnectionManager;
    private final WebSocketMessageHandler messageHandler;
    private final WebSocketMessageSender messageSender;

    public BaseWebSocketSessionClient(WebSocketClient webSocketClient,
                                      ObjectMapper objectMapper,
                                      ScheduledExecutorService scheduler,
                                      URI serverUri) {
        this.webSocketClient = webSocketClient;
        this.objectMapper = objectMapper;
        this.serverUri = serverUri;
        this.connectionState = new ConnectionStateManager();
        this.heartbeatManager = new HeartbeatManager(scheduler, serverUri, this::handleConnectionLoss);
        this.reconnectionManager = new ReconnectionManager(scheduler, serverUri, this::performReconnect);
        this.messageHandler = new WebSocketMessageHandler(serverUri, this::processMessage);
        this.messageSender = new WebSocketMessageSender(serverUri, this::handleConnectionLoss);
    }

    public void connect() {
        if (connectionState.isConnected()) {
            log.debug("Already connected to {}", serverUri);
            return;
        }

        try {
            log.info("Connecting to WebSocket: {}", serverUri);
            webSocketClient.execute(this, null, serverUri).get();
        } catch (Exception e) {
            log.error("Failed to connect to {}", serverUri, e);
            reconnectionManager.scheduleReconnect();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        this.session = session;
        this.session.setTextMessageSizeLimit(MAX_BUFFER_SIZE);
        connectionState.setConnected(true);
        reconnectionManager.resetAttempts();
        reconnectionManager.cancelReconnect();

        log.info("WebSocket connected to: {}", serverUri);

        heartbeatManager.startHeartbeat(session);

        // Perform connection-specific initialization
        try {
            onConnectionEstablished(session);
        } catch (Exception e) {
            log.error("Error in connection established callback", e);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        messageHandler.handleMessage(message);
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
        connectionState.setConnected(false);
        heartbeatManager.stopHeartbeat();

        // Perform cleanup
        try {
            onConnectionLost();
        } catch (Exception e) {
            log.error("Error in connection lost callback", e);
        }

        // Schedule reconnect if enabled
        if (connectionState.shouldReconnect()) {
            reconnectionManager.scheduleReconnect();
        }
    }

    private void performReconnect() {
        if (connectionState.shouldReconnect() && !connectionState.isConnected()) {
            connect();
        }
    }

    protected void sendMessage(String message) {
        messageSender.sendMessage(session, message);
    }

    public void disconnect() {
        connectionState.setShouldReconnect(false);
        reconnectionManager.cancelReconnect();
        heartbeatManager.stopHeartbeat();

        closeSession();

        connectionState.setConnected(false);
        log.info("Disconnected from {}", serverUri);
    }

    private void closeSession() {
        if (session != null && session.isOpen()) {
            try {
                session.close(CloseStatus.NORMAL);
            } catch (IOException e) {
                log.error("Error closing WebSocket session for {}", serverUri, e);
            }
        }
    }

    public boolean isConnected() {
        return connectionState.isConnected() && session != null && session.isOpen();
    }

    protected abstract void onConnectionEstablished(WebSocketSession session);
    protected abstract void processMessage(String message) throws Exception;
    protected abstract void onConnectionLost();
}
package com.blockchain.blockpulseservice.wsclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

@Slf4j
public abstract class BaseWebSocketSessionClient implements WebSocketHandler {
    private WebSocketSession session;
    protected final URI serverUri;
    private final int messageSizeLimit;
    private final WebSocketClient webSocketClient;
    private final ConnectionStateManager connectionState;
    private final ReconnectionManager reconnectionManager;
    private final WebSocketMessageHandler messageHandler;
    private final WebSocketMessageSender messageSender;
    protected final ObjectMapper objectMapper;

    protected BaseWebSocketSessionClient(WebSocketClient webSocketClient,
                                         URI serverUri,
                                         WebSocketSession session,
                                         @Value("${app.websocket.message.size.limit}")
                                         int messageSizeLimit,
                                         ConnectionStateManager connectionState,
                                         ReconnectionManager reconnectionManager,
                                         WebSocketMessageHandler messageHandler,
                                         WebSocketMessageSender messageSender,
                                         ObjectMapper objectMapper) {
        this.webSocketClient = webSocketClient;
        this.serverUri = serverUri;
        this.session = session;
        this.messageSizeLimit = messageSizeLimit;
        this.connectionState = connectionState;
        this.reconnectionManager = reconnectionManager;
        this.messageHandler = messageHandler;
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
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
            reconnectionManager.scheduleReconnect(this::reconnectCallback, serverUri);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        this.session = session;
        this.session.setTextMessageSizeLimit(messageSizeLimit);
        connectionState.setConnected(true);
        reconnectionManager.resetAttempts();
        reconnectionManager.cancelReconnect();

        log.info("WebSocket connected to: {}", serverUri);

        try {
            onConnectionEstablished(session);
        } catch (Exception e) {
            log.error("Error in connection established callback", e);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        Consumer<String> consumer = this::processMessage;
        messageHandler.handleMessage(message, consumer, serverUri);
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

    // WebSocketMessageSender
    private void handleConnectionLoss() {
        connectionState.setConnected(false);

        // Schedule reconnect if enabled
        if (connectionState.shouldReconnect()) {
            reconnectionManager.scheduleReconnect(this::reconnectCallback, serverUri);
        }
    }

    private Runnable reconnectCallback() {
        return () -> {
            if (connectionState.shouldReconnect() && !connectionState.isConnected()) {
                connect();
            }
        };
    }

    protected void sendMessage(String message) {
        messageSender.sendMessage(session, message, serverUri);
    }

    public void disconnect() {
        connectionState.setShouldReconnect(false);
        reconnectionManager.cancelReconnect();

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

    protected abstract void processMessage(String message);
}
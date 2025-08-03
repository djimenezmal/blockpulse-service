package com.blockchain.blockpulseservice.client.ws;

import com.blockchain.blockpulseservice.client.ws.manager.ConnectionStateManager;
import com.blockchain.blockpulseservice.client.ws.manager.ReconnectionManager;
import lombok.extern.slf4j.Slf4j;
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
    private final WebSocketClient webSocketClient;
    private final ConnectionStateManager connectionState;
    private final ReconnectionManager reconnectionManager;
    private final WebSocketMessageHandler messageHandler;
    private final WebSocketMessageSender messageSender;
    private final int messageSizeLimit;

    protected BaseWebSocketSessionClient(URI serverUri,
                                         int messageSizeLimit,
                                         WebSocketClient webSocketClient,
                                         ConnectionStateManager connectionState,
                                         ReconnectionManager reconnectionManager,
                                         WebSocketMessageHandler messageHandler,
                                         WebSocketMessageSender messageSender) {
        this.serverUri = serverUri;
        this.messageSizeLimit = messageSizeLimit;
        this.webSocketClient = webSocketClient;
        this.connectionState = connectionState;
        this.reconnectionManager = reconnectionManager;
        this.messageHandler = messageHandler;
        this.messageSender = messageSender;
    }


    public void connect() {
        if (isConnected()) {
            log.debug("Already connected to {}", serverUri);
            return;
        }

        try {
            log.info("Connecting to WebSocket: {}", serverUri);
            webSocketClient.execute(this, null, serverUri).get();
        } catch (Exception e) {
            log.error("Failed to connect to {}", serverUri, e);
            reconnectionManager.scheduleReconnect(this::connect, serverUri);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        this.session = session;
        this.session.setTextMessageSizeLimit(messageSizeLimit);
        connectionState.setConnected(true);

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

    private void handleConnectionLoss() {
        connectionState.setConnected(false);
        reconnectionManager.scheduleReconnect(this::connect, serverUri);
    }

    protected void sendMessage(String message) {
        messageSender.sendMessage(session, message, serverUri, this::connect);
    }

    public void disconnect() {
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

    private boolean isConnected() {
        return connectionState.isConnected() && session != null && session.isOpen();
    }

    protected abstract void onConnectionEstablished(WebSocketSession session);
    protected abstract void processMessage(String message);
}
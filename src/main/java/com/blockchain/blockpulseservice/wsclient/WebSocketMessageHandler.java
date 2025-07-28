package com.blockchain.blockpulseservice.wsclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

@Slf4j
public class WebSocketMessageHandler {
    private final URI serverUri;
    private final MessageProcessor messageProcessor;
    private final BinaryMessageHandler binaryMessageHandler;

    public WebSocketMessageHandler(URI serverUri, MessageProcessor messageProcessor, BinaryMessageHandler binaryMessageHandler) {
        this.serverUri = serverUri;
        this.messageProcessor = messageProcessor;
        this.binaryMessageHandler = binaryMessageHandler;
    }

    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            if (message instanceof TextMessage) {
                String payload = ((TextMessage) message).getPayload();
                log.debug("Received message from {}: {}", serverUri, payload.substring(0, Math.min(100, payload.length())));
                messageProcessor.processMessage(payload);
            } else if (message instanceof BinaryMessage) {
                binaryMessageHandler.handleBinaryMessage(session, (BinaryMessage) message);
            } else if (message instanceof PongMessage) {
                log.debug("Received pong from {}", serverUri);
            }
        } catch (Exception e) {
            log.error("Error processing message from {}", serverUri, e);
        }
    }

    public interface MessageProcessor {
        void processMessage(String message) throws Exception;
    }

    public interface BinaryMessageHandler {
        void handleBinaryMessage(WebSocketSession session, BinaryMessage message);
    }
}
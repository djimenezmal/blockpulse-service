package com.blockchain.blockpulseservice.wsclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

@Slf4j
public class WebSocketMessageHandler {
    private final URI serverUri;
    private final MessageProcessor messageProcessor;

    public WebSocketMessageHandler(URI serverUri, MessageProcessor messageProcessor) {
        this.serverUri = serverUri;
        this.messageProcessor = messageProcessor;
    }

    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.debug("RECEIVED message from {}: {}", serverUri, message.getClass().getSimpleName());
        try {
            if (message instanceof TextMessage textMessage) {
                var payload = textMessage.getPayload();
                log.debug("Received message from {}: {}", serverUri, payload.substring(0, Math.min(100, payload.length())));
                messageProcessor.processMessage(payload);
            } else if (message instanceof PongMessage) {
                log.debug("Received pong from {}", serverUri);
            }
        } catch (Exception e) {
            log.error("Error processing message from {}", serverUri, e);
        }
    }
}
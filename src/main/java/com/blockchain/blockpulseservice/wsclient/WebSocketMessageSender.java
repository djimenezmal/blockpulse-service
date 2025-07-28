package com.blockchain.blockpulseservice.wsclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URI;

@Slf4j
public class WebSocketMessageSender {
    private final URI serverUri;
    private final Runnable onSendFailure;

    public WebSocketMessageSender(URI serverUri, Runnable onSendFailure) {
        this.serverUri = serverUri;
        this.onSendFailure = onSendFailure;
    }

    public void sendMessage(WebSocketSession session, String message) {
        if (session == null || !session.isOpen()) {
            log.warn("Cannot send message - session not open for {}", serverUri);
            return;
        }

        try {
            session.sendMessage(new TextMessage(message));
            log.debug("Sent message to {}: {}", serverUri, message);
        } catch (IOException e) {
            log.error("Failed to send message to {}: {}", serverUri, message, e);
            onSendFailure.run();
        }
    }

    public void sendBinaryMessage(WebSocketSession session, byte[] data) {
        if (session == null || !session.isOpen()) {
            log.warn("Cannot send binary message - session not open for {}", serverUri);
            return;
        }

        try {
            session.sendMessage(new BinaryMessage(data));
            log.debug("Sent binary message to {} ({} bytes)", serverUri, data.length);
        } catch (IOException e) {
            log.error("Failed to send binary message to {}", serverUri, e);
            onSendFailure.run();
        }
    }
}
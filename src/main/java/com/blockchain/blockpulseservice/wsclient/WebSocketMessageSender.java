package com.blockchain.blockpulseservice.wsclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Component
public class WebSocketMessageSender {
    private final Runnable onSendFailure;

    public WebSocketMessageSender(Runnable onSendFailure) {
        this.onSendFailure = onSendFailure;
    }

    public void sendMessage(WebSocketSession session, String message, URI serverUri) {
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
}
package com.blockchain.blockpulseservice.client.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;

import java.net.URI;
import java.util.function.Consumer;

@Slf4j
@Component
public class WebSocketMessageHandler {
    public void handleMessage(WebSocketMessage<?> message, Consumer<String> messageConsumer, URI serverUri) {
        if (message instanceof TextMessage textMessage) {
            var payload = textMessage.getPayload();
            log.debug("Received message from {}: {}", serverUri, payload.substring(0, Math.min(200, payload.length())));
            messageConsumer.accept(payload);
        }
    }
}
package com.blockchain.blockpulseservice.wsconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.websocket.reconnect")
public record WebSocketReconnectionProperties(
        int maxAttempts,
        int initialDelaySeconds,
        int maxDelaySeconds
) {}
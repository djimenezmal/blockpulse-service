package com.blockchain.blockpulseservice.wsclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class ConnectionStateManager {
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean shouldReconnect = new AtomicBoolean(true);
    
    public boolean isConnected() {
        return connected.get();
    }
    
    public void setConnected(boolean connected) {
        this.connected.set(connected);
        log.debug("Connection state changed to: {}", connected);
    }
    
    public boolean shouldReconnect() {
        return shouldReconnect.get();
    }
    
    public void setShouldReconnect(boolean shouldReconnect) {
        this.shouldReconnect.set(shouldReconnect);
    }
}
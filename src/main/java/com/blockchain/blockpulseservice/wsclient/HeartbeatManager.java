package com.blockchain.blockpulseservice.wsclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HeartbeatManager {
    private static final int HEARTBEAT_INTERVAL = 30; // seconds

    private final ScheduledExecutorService scheduler;
    private final URI serverUri;
    private final Runnable onHeartbeatFailure;
    private ScheduledFuture<?> heartbeatTask;

    public HeartbeatManager(ScheduledExecutorService scheduler, URI serverUri, Runnable onHeartbeatFailure) {
        this.scheduler = scheduler;
        this.serverUri = serverUri;
        this.onHeartbeatFailure = onHeartbeatFailure;
    }

    public void startHeartbeat(WebSocketSession session) {
        stopHeartbeat();

        heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new PingMessage());
                    log.debug("Sent ping to {}", serverUri);
                } catch (IOException e) {
                    log.error("Failed to send heartbeat ping to {}", serverUri, e);
                    onHeartbeatFailure.run();
                }
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);

        log.debug("Started heartbeat for {}", serverUri);
    }

    public void stopHeartbeat() {
        if (heartbeatTask != null && !heartbeatTask.isDone()) {
            heartbeatTask.cancel(false);
            log.debug("Stopped heartbeat for {}", serverUri);
        }
    }
}
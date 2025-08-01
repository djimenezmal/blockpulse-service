package com.blockchain.blockpulseservice.wsclient;


import com.blockchain.blockpulseservice.wsconfig.WebSocketReconnectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class ReconnectionManager {

    private final WebSocketReconnectionProperties reconnectionProperties;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private ScheduledFuture<?> reconnectTask;

    public ReconnectionManager(ScheduledExecutorService scheduler,
                               WebSocketReconnectionProperties reconnectionProperties) {
        this.scheduler = scheduler;
        this.reconnectionProperties = reconnectionProperties;
    }

    public void scheduleReconnect(Runnable reconnectCallback, URI serverUri) {
        int attempts = reconnectAttempts.incrementAndGet();

        if (attempts > reconnectionProperties.maxAttempts()) {
            log.error("Max reconnect attempts ({}) reached for {}. Giving up.", reconnectionProperties.maxAttempts(), serverUri);
            return;
        }

        int delay = Math.min(
                reconnectionProperties.initialDelaySeconds() * (int) Math.pow(2, attempts - 1),
                reconnectionProperties.maxDelaySeconds()
        );
        delay += (int) (Math.random() * 5); // Add jitter

        log.info("Scheduling reconnect attempt {} for {} in {} seconds", attempts, serverUri, delay);

        reconnectTask = scheduler.schedule(reconnectCallback, delay, TimeUnit.SECONDS);
    }

    public void resetAttempts() {
        reconnectAttempts.set(0);
    }

    public void cancelReconnect() {
        if (reconnectTask != null && !reconnectTask.isDone()) {
            reconnectTask.cancel(false);
        }
    }
}
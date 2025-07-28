package com.blockchain.blockpulseservice.wsclient;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ReconnectionManager {
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final int INITIAL_RECONNECT_DELAY = 5;
    private static final int MAX_RECONNECT_DELAY = 300; // 5 minutes

    private final ScheduledExecutorService scheduler;
    private final URI serverUri;
    private final Runnable reconnectCallback;
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private ScheduledFuture<?> reconnectTask;

    public ReconnectionManager(ScheduledExecutorService scheduler, URI serverUri, Runnable reconnectCallback) {
        this.scheduler = scheduler;
        this.serverUri = serverUri;
        this.reconnectCallback = reconnectCallback;
    }

    public void scheduleReconnect() {
        int attempts = reconnectAttempts.incrementAndGet();

        if (attempts > MAX_RECONNECT_ATTEMPTS) {
            log.error("Max reconnect attempts ({}) reached for {}. Giving up.", MAX_RECONNECT_ATTEMPTS, serverUri);
            return;
        }

        // Exponential backoff with jitter
        int delay = Math.min(INITIAL_RECONNECT_DELAY * (int) Math.pow(2, attempts - 1), MAX_RECONNECT_DELAY);
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

    public int getAttemptCount() {
        return reconnectAttempts.get();
    }
}
package com.blockchain.blockpulseservice.wsclient;


import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ReconnectionManager {

    private final ScheduledExecutorService scheduler;
    private final RetryTemplate retryTemplate;
    private ScheduledFuture<?> reconnectTask;

    public ReconnectionManager(ScheduledExecutorService scheduler, RetryTemplate retryTemplate) {
        this.scheduler = scheduler;
        this.retryTemplate = retryTemplate;
    }

    public void scheduleReconnect(Runnable reconnectCallback, URI serverUri) {
        log.info("Scheduling reconnection...");
        reconnectTask = scheduler.schedule(() -> {
            retryTemplate.execute(context -> {
                log.info("Reconnect attempt {} for {}", context.getRetryCount() + 1, serverUri);
                reconnectCallback.run();
                return null;
            });
        }, 3, TimeUnit.SECONDS);
    }

    public void cancelReconnect() {
        if (reconnectTask != null && !reconnectTask.isDone()) {
            reconnectTask.cancel(false);
        }
    }
}
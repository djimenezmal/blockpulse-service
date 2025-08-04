package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.Transaction;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SlidingWindowManager {
    private final TreeSet<Transaction> feeRatesMap = new TreeSet<>();
    private final BlockingQueue<Transaction> transactionQueue = new LinkedBlockingQueue<>();
    private final TransactionAnalyzerService analyzerService;
    private final ThreadFactory analyzerThreadFactory;
    private final int slidingWindowSize;
    private volatile AtomicBoolean running = new AtomicBoolean(true);
    private Thread analyzerThread;

    public SlidingWindowManager(@Value("${app.analysis.tx.sliding-window-size}") int slidingWindowSize,
                                TransactionAnalyzerService analyzerService,
                                ThreadFactory analyzerThreadFactory) {
        this.slidingWindowSize = slidingWindowSize;
        this.analyzerService = analyzerService;
        this.analyzerThreadFactory = analyzerThreadFactory;
    }

    @PostConstruct
    private void startAnalyzerThread() {
        analyzerThread = analyzerThreadFactory.newThread(() -> {
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                Transaction tx = null;
                try {
                    tx = transactionQueue.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running.set(false);
                }
                if (tx == null) {
                    analyzerService.processTransaction(tx, feeRatesMap);
                }
            }
        });
        analyzerThread.start();
    }

    @PreDestroy
    private void stopAnalyzerThread() {
        running.set(false);
        if (analyzerThread != null) {
            analyzerThread.interrupt();
            try {
                analyzerThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void add(List<Transaction> newTxs) {
        newTxs.forEach(tx -> {
            if (transactionQueue.size() >= slidingWindowSize) {
                var oldestTx = transactionQueue.poll();
                feeRatesMap.remove(oldestTx);
            }
            if (transactionQueue.offer(tx)) {
                feeRatesMap.add(tx);
            }
        });
    }
}
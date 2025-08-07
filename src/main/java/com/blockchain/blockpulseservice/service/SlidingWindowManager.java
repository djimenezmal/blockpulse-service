package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.Transaction;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.list.TreeList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class SlidingWindowManager {
    private final TreeList<Transaction> transactionsPerFeeRate = new TreeList<>();
    private final BlockingQueue<Transaction> transactionQueue = new LinkedBlockingQueue<>();
    private final int slidingWindowSize;
    private final TransactionAnalyzerService analyzerService;
    private final ThreadFactory analyzerThreadFactory;
    private final TransactionWindowSnapshotService transactionWindowSnapshotService;
    private Thread analyzerThread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public SlidingWindowManager(@Value("${app.analysis.tx.sliding-window-size:1000}") int slidingWindowSize,
                                TransactionAnalyzerService analyzerService,
                                ThreadFactory analyzerThreadFactory,
                                TransactionWindowSnapshotService transactionWindowSnapshotService) {
        this.slidingWindowSize = slidingWindowSize;
        this.analyzerService = analyzerService;
        this.analyzerThreadFactory = analyzerThreadFactory;
        this.transactionWindowSnapshotService = transactionWindowSnapshotService;
    }

    @PostConstruct
    private void startAnalyzerThread() {
        analyzerThread = analyzerThreadFactory.newThread(() -> {
            log.info("Started analyzer thread {}", analyzerThread.getName());
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                Transaction tx = null;
                try {
                    tx = transactionQueue.take();
                } catch (InterruptedException e) {
                    log.warn("Thread interrupted while waiting for transaction", e);
                    running.set(false);
                    Thread.currentThread().interrupt();
                }
                if (tx != null) {
                    var snapshot = transactionWindowSnapshotService.takeCurrentWindowSnapshot(transactionsPerFeeRate);
                    analyzerService.processTransaction(tx, snapshot);
                }
            }
        });
        analyzerThread.start();
    }

    @PreDestroy
    private void stopAnalyzerThread() {
        running.set(false);
        if (analyzerThread != null) {
            log.info("Stopping analyzer thread {}", analyzerThread.getName());
            analyzerThread.interrupt();
            try {
                analyzerThread.join(5000);
            } catch (InterruptedException e) {
                log.error("Error stopping analyzer thread {}", analyzerThread.getName(), e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void addTransaction(List<Transaction> transactions) {
        transactions.forEach(newTx -> {
            if (isValidTransaction(newTx)) {
                if (transactionQueue.size() >= slidingWindowSize) {
                    log.debug("Sliding window is full, removing oldest transaction: {}", transactionsPerFeeRate.getFirst().hash());
                    var oldestTx = transactionQueue.poll();
                    transactionsPerFeeRate.remove(oldestTx);
                    transactionWindowSnapshotService.subtractFee(oldestTx.feePerVSize());
                }
                if (transactionQueue.offer(newTx)) {
                    log.debug("Added transaction to sliding window: {}", newTx.hash());
                    transactionsPerFeeRate.add(newTx);
                    transactionWindowSnapshotService.addFee(newTx.feePerVSize());
                }
            }
        });
    }

    private boolean isValidTransaction(Transaction tx) {
        if (tx.feePerVSize() < 0) {
            log.warn("Invalid fee rate: {}", tx.feePerVSize());
            return false;
        }
        if (tx.size() <= 0) {
            log.warn("Invalid transaction size: {}", tx.size());
            return false;
        }
        return true;
    }
}
package com.blockchain.blockpulseservice.service.sliding_window;

import com.blockchain.blockpulseservice.model.Transaction;
import com.blockchain.blockpulseservice.service.TransactionAnalyzerService;
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

import static java.math.BigDecimal.ZERO;

@Slf4j
@Component
public class SlidingWindowManager {
    private final TreeList<Transaction> sortedTransactions = new TreeList<>();
    private final BlockingQueue<Transaction> transactionQueue = new LinkedBlockingQueue<>();
    private final int slidingWindowSize;
    private final TransactionAnalyzerService analyzerService;
    private final TransactionWindowSnapshotService transactionWindowSnapshotService;
    private final ThreadFactory analyzerThreadFactory;
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
                try {
                    var tx = transactionQueue.take();

                    resizeSortedTransactionsPerFeeRate();

                    sortedTransactions.add(tx);
                    transactionWindowSnapshotService.addFee(tx.feePerVSize());

                    var snapshot = transactionWindowSnapshotService.takeCurrentWindowSnapshot(sortedTransactions);
                    analyzerService.processTransaction(tx, snapshot);
                } catch (InterruptedException e) {
                    log.warn("Thread interrupted while waiting for transaction", e);
                    running.set(false);
                    Thread.currentThread().interrupt();
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
        transactions.stream()
                .filter(this::isValidTransaction)
                .forEach(tx -> {
                    log.error("XQUE {}", tx);
                    if (transactionQueue.offer(tx)) {
                        log.debug("Queued transaction for analysis: {}", tx.hash());
                    }
                });
    }

    private void resizeSortedTransactionsPerFeeRate() {
        if (sortedTransactions.size() >= slidingWindowSize) {
            var oldestTx = sortedTransactions.removeFirst();
            transactionWindowSnapshotService.subtractFee(oldestTx.feePerVSize());
            log.debug("Sliding window is full, removing oldest transaction: {}", oldestTx.hash());
        }
    }

    private boolean isValidTransaction(Transaction tx) {
        if (tx.feePerVSize().compareTo(ZERO) < 0) {
            log.warn("Invalid fee rate: {}", tx.feePerVSize());
            return false;
        }
        if (tx.vSize() <= 0) {
            log.warn("Invalid transaction vSize: {}", tx.vSize());
            return false;
        }
        return true;
    }
}
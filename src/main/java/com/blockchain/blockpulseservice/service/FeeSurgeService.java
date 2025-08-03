package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.event.MempoolDataUpdatedEvent;
import com.blockchain.blockpulseservice.model.MempoolStats;
import com.blockchain.blockpulseservice.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FeeSurgeService {
    private static final int WINDOW_SIZE_DEFAULT = 1000;
    private final TreeSet<Transaction> feeRatesMap = new TreeSet<>();
    private final Queue<Transaction> transactionQueue = new LinkedList<>();
    private final SimpMessagingTemplate messagingTemplate;

    public void processTransaction(Transaction newTx) {
        if (transactionQueue.size() >= WINDOW_SIZE_DEFAULT) {
            var oldestTx = transactionQueue.poll();
            feeRatesMap.remove(oldestTx);
        }
        transactionQueue.offer(newTx);
        feeRatesMap.add(newTx);
        if (feeRatesMap.size() < 10) {
            return;
        }

        boolean isSurge = newTx.feeRate() > getCurrentPercentile99();
        if (isSurge) {
            log.info("Surge detected for tx: {}", newTx.hash());
        }
        messagingTemplate.convertAndSend("/topic/transactions", newTx);
    }

    @EventListener
    public void onMempoolDataUpdated(MempoolDataUpdatedEvent event) {
        var mempoolStats = event.getMempoolStats();
        log.info("Received updated mempool stats: {}", mempoolStats);
    }

    private double getCurrentPercentile99() {
        int index = (int) Math.ceil(0.99 * feeRatesMap.size()) - 1;
        var sortedArray = feeRatesMap.toArray(new Transaction[0]);
        return sortedArray[index].feeRate();
    }
}
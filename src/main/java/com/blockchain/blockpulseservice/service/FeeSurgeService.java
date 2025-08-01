package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.tx.Transaction;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

@Service
public class FeeSurgeService {
    private static final int WINDOW_SIZE_DEFAULT = 1000;
    private final TreeSet<Transaction> feeRatesMap = new TreeSet<>();
    private final Queue<Transaction> transactionQueue = new LinkedList<>();

    public void processTransaction(Transaction newTx ) {
        if (transactionQueue.size() >= WINDOW_SIZE_DEFAULT) {
            Transaction oldest = transactionQueue.poll();
            feeRatesMap.remove(oldest);
        }
        transactionQueue.offer(newTx);
        feeRatesMap.add(newTx);
        if (feeRatesMap.size() < 10) {
            return;
        }

        boolean isSurge = newTx.feeRate() > getCurrentPercentile99();
    }

    public double getCurrentPercentile99() {
        int index = (int) Math.ceil(0.99 * feeRatesMap.size()) - 1;
        Transaction[] sortedArray = feeRatesMap.toArray(new Transaction[0]);
        return sortedArray[index].feeRate();
    }
}

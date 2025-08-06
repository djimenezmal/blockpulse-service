package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.Transaction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class TransactionWindowSnapshot {
    @Getter
    private final int totalTransactions;
    @Getter
    private final double averageFeeRatePerVSize;
    @Getter
    private final double medianFeeRatePerVSize;
    private final List<Transaction> transactions;

    public static TransactionWindowSnapshot empty() {
        return new TransactionWindowSnapshot(0, 0.0, 0.0, Collections.emptyList());
    }

    public boolean isEmpty() {
        return totalTransactions == 0;
    }

    public double getPercentileFeeRate(double percentile) {
        if (transactions.isEmpty() || percentile < 0 || percentile > 100) {
            return 0.0;
        }
        int index = (int) Math.ceil(percentile / 100.0 * transactions.size()) - 1;
        return transactions.get(Math.max(0, index)).feePerVSize();
    }
}
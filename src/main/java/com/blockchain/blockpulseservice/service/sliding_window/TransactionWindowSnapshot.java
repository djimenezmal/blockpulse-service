package com.blockchain.blockpulseservice.service.sliding_window;

import com.blockchain.blockpulseservice.model.Transaction;

import java.util.Collections;
import java.util.List;

public record TransactionWindowSnapshot(int totalTransactions,
                                        double averageFeeRatePerVSize,
                                        double medianFeeRatePerVSize, int numOfOutliers,
                                        double outlierFeeRatePercentile,
                                        List<Transaction> transactions) {
    public static TransactionWindowSnapshot empty() {
        return new TransactionWindowSnapshot(0, 0.0, 0.0, 0, 0.0, Collections.emptyList());
    }

    public double getPercentileFeeRate(double percentile) {
        int index = getPercentileIndex(percentile);
        return transactions.get(Math.max(0, index)).feePerVSize();
    }

    private int getPercentileIndex(double percentile) {
        return (int) Math.ceil(percentile / 100 * totalTransactions) - 1;
    }
}
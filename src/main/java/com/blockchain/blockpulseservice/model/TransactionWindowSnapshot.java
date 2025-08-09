package com.blockchain.blockpulseservice.model;

import java.util.Collections;
import java.util.List;

public record TransactionWindowSnapshot(int totalTransactions,
                                        double averageFeeRatePerVSize,
                                        double medianFeeRatePerVSize,
                                        int numOfOutliers,
                                        double outlierFeeRatePercentile,
                                        double firstQuartile,
                                        double thirdQuartile,
                                        List<Transaction> transactions) {
    public static TransactionWindowSnapshot empty() {
        return new TransactionWindowSnapshot(0, 0.0, 0.0, 0, 0.0, 0.0,0.0, Collections.emptyList());
    }
}
package com.blockchain.blockpulseservice.service.sliding_window;

import com.blockchain.blockpulseservice.model.Transaction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@RequiredArgsConstructor
public class TransactionWindowSnapshot {
    @Getter
    int totalTransactions;
    @Getter
    double averageFeeRatePerVSize;
    @Getter
    double medianFeeRatePerVSize;
    int numOfOutliers;
    List<Transaction> transactions;

    public static TransactionWindowSnapshot empty() {
        return new TransactionWindowSnapshot(0, 0.0, 0.0, 0, Collections.emptyList());
    }

    public double getPercentileFeeRate(double percentile) {
        int index = getPercentileIndex(percentile);
        return transactions.get(Math.max(0, index)).feePerVSize();
    }

    private int getPercentileIndex(double percentile) {
        return (int) Math.ceil(percentile / 100 * totalTransactions) - 1;
    }
}
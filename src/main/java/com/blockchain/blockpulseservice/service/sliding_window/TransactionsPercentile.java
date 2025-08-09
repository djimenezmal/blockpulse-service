package com.blockchain.blockpulseservice.service.sliding_window;

import com.blockchain.blockpulseservice.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TransactionsPercentile {
    public double getPercentileFeeRate(double percentile, List<Transaction> transactions) {
        int index = Math.max(0, getPercentileIndex(percentile, transactions.size()));
        try {
            return transactions.get(index).feePerVSize();
        } catch (Exception e) {
            log.error("Error getting percentile fee rate for index: {}. Transactions size is: {}", index, transactions.size(), e);
            System.exit(1);
            return -1;
        }
    }

    public int getNumOfOutliers(double outliersPercentileThreshold, int totalTransactions) {
        return getPercentileIndex(outliersPercentileThreshold, totalTransactions);
    }

    public double getMedianFeeRate(List<Transaction> transactions) {
        int size = transactions.size();
        if (size % 2 == 0) {
            return (transactions.get(size / 2 - 1).feePerVSize() + transactions.get(size / 2).feePerVSize()) / 2.0;
        } else {
            return transactions.get(size / 2).feePerVSize();
        }
    }

    private int getPercentileIndex(double percentile, int totalTransactions) {
        return (int) Math.ceil(percentile * totalTransactions) - 1;
    }
}
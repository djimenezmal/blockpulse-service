package com.blockchain.blockpulseservice.service.sliding_window;

import com.blockchain.blockpulseservice.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
class TransactionWindowSnapshotService {
    private final TransactionsPercentile percentile;
    private final double outliersPercentileThreshold;
    private double sum = 0;

    public TransactionWindowSnapshotService(TransactionsPercentile percentile,
                                            @Value("${app.analysis.tx.outliers-percentile-threshold:0.99}") double outliersPercentileThreshold) {
        this.percentile = percentile;
        this.outliersPercentileThreshold = outliersPercentileThreshold;
    }

    public void addFee(double feePerVSize) {
        sum += feePerVSize;
    }

    public void subtractFee(double feePerVSize) {
        sum -= feePerVSize;
    }

    public TransactionWindowSnapshot takeCurrentWindowSnapshot(List<Transaction> transactionsPerFeeRate) {
        log.debug("Taking current window snapshot...");
        if (transactionsPerFeeRate.isEmpty()) {
            log.debug("No transactions in window, returning empty snapshot");
            return TransactionWindowSnapshot.empty();
        }

        int totalTransactions = transactionsPerFeeRate.size();
        double averageFeeRate = sum / totalTransactions;
        return new TransactionWindowSnapshot(
                totalTransactions,
                averageFeeRate,
                getMedianFeeRate(transactionsPerFeeRate),
                getNumOfOutliers(transactionsPerFeeRate),
                percentile.getPercentileFeeRate(outliersPercentileThreshold, transactionsPerFeeRate),
                transactionsPerFeeRate
        );
    }

    private double getMedianFeeRate(List<Transaction> transactionsPerFeeRate) {
        int size = transactionsPerFeeRate.size();
        if (size % 2 == 0) {
            return (transactionsPerFeeRate.get(size / 2 - 1).feePerVSize() + transactionsPerFeeRate.get(size / 2).feePerVSize()) / 2.0;
        } else {
            return transactionsPerFeeRate.get(size / 2).feePerVSize();
        }
    }
    public int getNumOfOutliers(List<Transaction> transactions) {
        int totalTransactions = transactions.size();
        return totalTransactions - percentile.getPercentileIndex(outliersPercentileThreshold, totalTransactions);
    }
}
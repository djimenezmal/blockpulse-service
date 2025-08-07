package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
class TransactionWindowSnapshotService {
    private double sum = 0;

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

    public double getPercentileFeeRate(double percentile, List<Transaction> transactionsPerFeeRate) {
        int index = (int) Math.ceil(percentile / 100 * transactionsPerFeeRate.size()) - 1;
        return transactionsPerFeeRate.get(Math.max(0, index)).feePerVSize();
    }
}
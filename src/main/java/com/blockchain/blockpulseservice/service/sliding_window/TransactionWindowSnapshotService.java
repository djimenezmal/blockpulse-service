package com.blockchain.blockpulseservice.service.sliding_window;

import com.blockchain.blockpulseservice.model.Transaction;
import com.blockchain.blockpulseservice.model.TransactionWindowSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
class TransactionWindowSnapshotService {
    private final TransactionsPercentile percentile;
    private final double outliersPercentileThreshold;
    private final double firstQuartileThreshold;
    private final double thirdQuartileThreshold;
    private double sum = 0;

    public TransactionWindowSnapshotService(TransactionsPercentile percentile,
                                            @Value("${app.analysis.tx.outliers-percentile-threshold:0.99}")
                                            double outliersPercentileThreshold,
                                            @Value("${app.analysis.tx.local-first-quartile:0.25}")
                                            double firstQuartileThreshold,
                                            @Value("${app.analysis.tx.local-third-quartile:0.75}")
                                            double thirdQuartileThreshold) {
        this.percentile = percentile;
        this.outliersPercentileThreshold = outliersPercentileThreshold;
        this.firstQuartileThreshold = firstQuartileThreshold;
        this.thirdQuartileThreshold = thirdQuartileThreshold;
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
                percentile.getMedianFeeRate(transactionsPerFeeRate),
                percentile.getNumOfOutliers(outliersPercentileThreshold, totalTransactions),
                percentile.getPercentileFeeRate(outliersPercentileThreshold, transactionsPerFeeRate),
                percentile.getPercentileFeeRate(firstQuartileThreshold, transactionsPerFeeRate),
                percentile.getPercentileFeeRate(thirdQuartileThreshold, transactionsPerFeeRate),
                transactionsPerFeeRate
        );
    }
}
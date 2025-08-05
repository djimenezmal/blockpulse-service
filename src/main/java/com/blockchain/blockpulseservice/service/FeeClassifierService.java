package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.FeeClassification;
import com.blockchain.blockpulseservice.model.MempoolStats;
import com.blockchain.blockpulseservice.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.TreeSet;

@Service
public class FeeClassifierService {
    private static final double LOCAL_CHEAP_PERCENTILE = 0.25;
    private static final double LOCAL_NORMAL_PERCENTILE = 0.75;
    private final TransactionStatistics stats;
    private final int mempoolSizeThreshold;

    public FeeClassifierService(TransactionStatistics stats,
                                ) {
        this.stats = stats;
        this.mempoolSizeThreshold = mempoolSizeThreshold;
    }

    public FeeClassification classifyFee(double txFeeRate, MempoolStats mempoolStats, TreeSet<Transaction> orderedTxsByFee) {
        if (mempoolStats.mempoolSize() > mempoolSizeThreshold) {
            // Network congested → use mempool recommended fees
            if (txFeeRate < mempoolStats.fastFee()) {
                return FeeClassification.CHEAP;
            } else if (txFeeRate <= mempoolStats.mediumFee()) {
                return FeeClassification.NORMAL;
            } else {
                return FeeClassification.EXPENSIVE;
            }
        } else {
            // Normal network → use local percentiles
            double p25 = stats.getCurrentPercentile(LOCAL_CHEAP_PERCENTILE, orderedTxsByFee);
            double p75 = stats.getCurrentPercentile(LOCAL_NORMAL_PERCENTILE, orderedTxsByFee);

            if (txFeeRate < p25) {
                return FeeClassification.CHEAP;
            } else if (txFeeRate <= p75) {
                return FeeClassification.NORMAL;
            } else {
                return FeeClassification.EXPENSIVE;
            }
        }
    }
}
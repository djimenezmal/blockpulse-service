package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.client.rest.MempoolStatsClient;
import com.blockchain.blockpulseservice.model.AnalyzedTransaction;
import com.blockchain.blockpulseservice.model.FeeClassification;
import com.blockchain.blockpulseservice.model.InsightType;
import com.blockchain.blockpulseservice.model.MempoolStats;
import com.blockchain.blockpulseservice.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.TreeSet;

@Slf4j
@Service
public class TransactionAnalyzerService {
    private static final double LOCAL_CHEAP_PERCENTILE = 0.25;
    private static final double LOCAL_NORMAL_PERCENTILE = 0.75;
    private final double outliersPercentileThreshold;
    private final int mempoolSizeThreshold;
    private final SimpMessagingTemplate messagingTemplate;
    private final MempoolStatsClient mempoolStatsClient;

    public TransactionAnalyzerService(
            @Value("${app.analysis.tx.outliers-percentile-threshold}") double outliersPercentileThreshold,
            @Value("${app.analysis.tx.mempool-congestion-vbytes-threshold}") int mempoolSizeThreshold,
            SimpMessagingTemplate messagingTemplate,
            MempoolStatsClient mempoolStatsClient) {
        this.outliersPercentileThreshold = outliersPercentileThreshold;
        this.mempoolSizeThreshold = mempoolSizeThreshold;
        this.messagingTemplate = messagingTemplate;
        this.mempoolStatsClient = mempoolStatsClient;
    }

    public void processTransaction(Transaction newTx, TreeSet<Transaction> orderedTxsByFee) {
        log.info("Processing transaction: {}", newTx);
        var insights = new ArrayList<InsightType>(InsightType.values().length);
        boolean isOutlier = newTx.feePerVSize() > getCurrentPercentile(outliersPercentileThreshold, orderedTxsByFee);
        if (isOutlier) {
            log.info("Outlier detected for tx: {}", newTx.hash());
            insights.add(InsightType.OUTLIER);
        }
        var mempoolStats = mempoolStatsClient.getMempoolStats();
        boolean isSurge = isOutlier && newTx.feePerVSize() > mempoolStats.fastFee() && mempoolStats.mempoolSize() > mempoolSizeThreshold;
        if (isSurge) {
            log.info("Surge detected for tx: {}", newTx.hash());
            insights.add(InsightType.SURGE);
        }
        var classification = classifyFee(newTx.feePerVSize(), mempoolStats, orderedTxsByFee);
        log.info("Classification for tx: {} is {}", newTx.hash(), classification);

        var analyzedTransaction = new AnalyzedTransaction(newTx.hash(),
                newTx.feePerVSize(),
                newTx.totalFee(),
                newTx.size(),
                newTx.time(),
                insights,
                classification);

        messagingTemplate.convertAndSend("/topic/transactions", analyzedTransaction);
        log.info("Sent transaction analysis message: {}", analyzedTransaction);
        //messagingTemplate.convertAndSend("/topic/tx-stats", feeSum / orderedTxsByFee.size());
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
            double p25 = getCurrentPercentile(LOCAL_CHEAP_PERCENTILE, orderedTxsByFee);
            double p75 = getCurrentPercentile(LOCAL_NORMAL_PERCENTILE, orderedTxsByFee);

            if (txFeeRate < p25) {
                return FeeClassification.CHEAP;
            } else if (txFeeRate <= p75) {
                return FeeClassification.NORMAL;
            } else {
                return FeeClassification.EXPENSIVE;
            }
        }
    }

    private double getCurrentPercentile(double percentile, TreeSet<Transaction> orderedTxsByFee) {
        int index = (int) Math.ceil(percentile * orderedTxsByFee.size()) - 1;
        var sortedArray = orderedTxsByFee.toArray(new Transaction[0]);
        return sortedArray[index].totalFee();
    }
}
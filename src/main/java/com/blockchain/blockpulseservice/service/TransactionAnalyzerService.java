package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.client.rest.MempoolStatsClient;
import com.blockchain.blockpulseservice.model.AnalyzedTransaction;
import com.blockchain.blockpulseservice.model.InsightType;
import com.blockchain.blockpulseservice.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.TreeSet;

@Slf4j
@Service
public class TransactionAnalyzerService {
      private final double outliersPercentileThreshold;
    private final NotificationService notificationService;
    private final FeeClassifierService feeClassifierService;
    private final MempoolStatsClient mempoolStatsClient;

    public TransactionAnalyzerService(
            @Value("${app.analysis.tx.outliers-percentile-threshold}") double outliersPercentileThreshold,
            NotificationService notificationService,
            FeeClassifierService feeClassifierService,
            MempoolStatsClient mempoolStatsClient) {
        this.outliersPercentileThreshold = outliersPercentileThreshold;
        this.notificationService = notificationService;
        this.feeClassifierService = feeClassifierService;
        this.mempoolStatsClient = mempoolStatsClient;
    }
    public void processTransaction(Transaction newTx, TreeSet<Transaction> orderedTxsByFee) {
        log.info("Processing transaction: {}", newTx);
        var insights = new ArrayList<InsightType>(InsightType.values().length);
        boolean isOutlier = newTx.feePerVSize() > stats.getCurrentPercentile(outliersPercentileThreshold, orderedTxsByFee);
        if (isOutlier) {
            log.info("Outlier detected for tx: {}", newTx.hash());
            insights.add(InsightType.OUTLIER);
        }
        var mempoolStats = mempoolStatsClient.getMempoolStats();
        boolean isSurge = isOutlier && newTx.feePerVSize() > mempoolStats.fastFee() && mempoolStats.mempoolSize() > mempoolSizeThreshold;
        if (isSurge) {
            insights.add(InsightType.SURGE);
        }

        var txFeeClassification = feeClassifierService.classifyFee(newTx.feePerVSize(), mempoolStats, orderedTxsByFee);
        log.info("Classification for tx: {} is {}", newTx.hash(), txFeeClassification);

        var analyzedTransaction = new AnalyzedTransaction(newTx.hash(),
                newTx.feePerVSize(),
                newTx.totalFee(),
                newTx.size(),
                newTx.time(),
                insights,
                txFeeClassification);

        notificationService.sendAnalysis(analyzedTransaction);
        //messagingTemplate.convertAndSend("/topic/tx-stats", feeSum / orderedTxsByFee.size());
    }
}
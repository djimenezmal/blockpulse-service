package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.client.rest.MempoolStatsUpdater;
import com.blockchain.blockpulseservice.model.AnalyzedTransactionDTO;
import com.blockchain.blockpulseservice.model.Transaction;
import com.blockchain.blockpulseservice.model.AnalysisContext;
import com.blockchain.blockpulseservice.service.analysis.TransactionAnalyzer;
import com.blockchain.blockpulseservice.model.TransactionWindowSnapshot;
import com.blockchain.blockpulseservice.model.TransactionWindowSnapshotDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionAnalyzerService {
    private final TransactionAnalyzer analysisChain;
    private final NotificationService notificationService;
    private final MempoolStatsUpdater mempoolStatsUpdater;

    public void processTransaction(Transaction transaction, TransactionWindowSnapshot transactionWindowSnapshot) {
        log.debug("Processing transaction: {}", transaction.hash());
        try {
            var context = AnalysisContext.builder()
                    .transaction(transaction)
                    .transactionWindowSnapshot(transactionWindowSnapshot)
                    .mempoolStats(mempoolStatsUpdater.getMempoolStats())
                    .build();

            var result = analysisChain.analyze(context);
            var analyzedTransaction = mapToAnalyzedTransaction(result);
            log.debug("Analyzed transaction: {}", analyzedTransaction);
            notificationService.sendAnalysis(analyzedTransaction);
        } catch (Exception e) {
            log.error("Failed to process transaction {}: {}", transaction.hash(), e.getMessage(), e);
        }
    }

    private AnalyzedTransactionDTO mapToAnalyzedTransaction(AnalysisContext context) {
        return AnalyzedTransactionDTO.builder()
                .id(context.getTransaction().hash())
                .feePerVByte(context.getTransaction().feePerVSize())
                .totalFee(context.getTransaction().totalFee())
                .size(context.getTransaction().size())
                .timestamp(context.getTransaction().time())
                .patterns(context.getPatterns())
                .feeClassification(context.getFeeClassification())
                .isOutlier(context.isOutlier())
                .windowSnapshotDTO(mapToTransactionWindowSnapshotDTO(context.getTransactionWindowSnapshot()))
                .build();
    }

    private TransactionWindowSnapshotDTO mapToTransactionWindowSnapshotDTO(TransactionWindowSnapshot windowSnapshot) {
        return new TransactionWindowSnapshotDTO(
                windowSnapshot.totalTransactions(),
                windowSnapshot.averageFeeRatePerVSize(),
                windowSnapshot.medianFeeRatePerVSize(),
                windowSnapshot.numOfOutliers());
    }
}
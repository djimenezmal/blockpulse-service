package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.client.rest.MempoolStatsUpdater;
import com.blockchain.blockpulseservice.model.AnalyzedTransactionDTO;
import com.blockchain.blockpulseservice.model.Transaction;
import com.blockchain.blockpulseservice.service.analysis.AnalysisContext;
import com.blockchain.blockpulseservice.service.analysis.TransactionAnalyzer;
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

    public void processTransaction(Transaction newTx, TransactionWindowSnapshot transactionWindowSnapshot) {
        log.debug("Processing transaction: {}", newTx.hash());
        try {
            var context = AnalysisContext.builder()
                    .transaction(newTx)
                    .transactionWindowSnapshot(transactionWindowSnapshot)
                    .mempoolStats(mempoolStatsUpdater.getMempoolStats())
                    .build();

            var result = analysisChain.analyze(context);
            var analyzedTransaction = mapToAnalyzedTransaction(result);
            log.debug("Analyzed transaction: {}", analyzedTransaction);
            notificationService.sendAnalysis(analyzedTransaction);
        } catch (Exception e) {
            log.error("Failed to process transaction {}: {}", newTx.hash(), e.getMessage(), e);
        }
    }

    private AnalyzedTransactionDTO mapToAnalyzedTransaction(AnalysisContext context) {
        return new AnalyzedTransactionDTO(
                context.getTransaction().hash(),
                context.getTransaction().feePerVSize(),
                context.getTransaction().totalFee(),
                context.getTransaction().size(),
                context.getTransaction().time(),
                context.getInsights(),
                context.getFeeClassification(),
                context.isOutlier()
        );
    }
}
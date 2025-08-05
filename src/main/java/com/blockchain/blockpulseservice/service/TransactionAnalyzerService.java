package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.client.rest.MempoolStatsClient;
import com.blockchain.blockpulseservice.model.AnalyzedTransaction;
import com.blockchain.blockpulseservice.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.TreeSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionAnalyzerService {
    private final TransactionAnalyzer analysisChain;
    private final NotificationService notificationService;
    private final MempoolStatsClient mempoolStatsClient;

    public void processTransaction(Transaction newTx, TreeSet<Transaction> orderedTxsByFee) {
        try {
            AnalysisContext context = AnalysisContext.builder()
                    .transaction(newTx)
                    .orderedTransactions(orderedTxsByFee)
                    .mempoolStats(mempoolStatsClient.getMempoolStats())
                    .build();

            // Run through the analysis chain
            AnalysisContext result = analysisChain.analyze(context);

            // Convert to your existing model and send
            var analyzedTransaction = convertToAnalyzedTransaction(result);
            notificationService.sendAnalysis(analyzedTransaction);

        } catch (Exception e) {
            log.error("Failed to process transaction {}: {}", newTx.hash(), e.getMessage(), e);
        }
    }

    private AnalyzedTransaction convertToAnalyzedTransaction(AnalysisContext context) {
        return new AnalyzedTransaction(
                context.getTransaction().hash(),
                context.getTransaction().feePerVSize(),
                context.getTransaction().totalFee(),
                context.getTransaction().size(),
                context.getTransaction().time(),
                new ArrayList<>(context.getInsights()),
                context.getFeeClassification()
        );
    }
}
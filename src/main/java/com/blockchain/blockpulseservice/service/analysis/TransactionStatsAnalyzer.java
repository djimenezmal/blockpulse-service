package com.blockchain.blockpulseservice.service.analysis;

import com.blockchain.blockpulseservice.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class TransactionStatsAnalyzer {
    protected AnalysisContext doAnalyze(AnalysisContext context) {
        var transactions = context.getSortedTransactionsPerFeeRate();
        int totalAnalyzedTransactions = transactions.length;
        double maxFeeRatePerVSize = transactions[totalAnalyzedTransactions - 1].feePerVSize();
        double minFeeRatePerVSize = transactions[0].feePerVSize();
        double averageFeeRatePerVSize = Arrays.stream(transactions).mapToDouble(Transaction::feePerVSize).sum() / totalAnalyzedTransactions;
        var transactionStatistics = new TransactionStatistics(averageFeeRatePerVSize, maxFeeRatePerVSize, minFeeRatePerVSize, totalAnalyzedTransactions);
        return context
                .addTransactionStatistics(transactionStatistics)
                .build();
    }
}
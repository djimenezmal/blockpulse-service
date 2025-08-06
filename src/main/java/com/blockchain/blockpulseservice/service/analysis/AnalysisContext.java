package com.blockchain.blockpulseservice.service.analysis;

import com.blockchain.blockpulseservice.model.FeeClassification;
import com.blockchain.blockpulseservice.model.InsightType;
import com.blockchain.blockpulseservice.model.MempoolStats;
import com.blockchain.blockpulseservice.model.Transaction;
import lombok.Builder;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;

@Builder(toBuilder = true)
@Value
public class AnalysisContext {
    Transaction transaction;
    Transaction[] sortedTransactionsPerFeeRate;
    MempoolStats mempoolStats;
    @Builder.Default
    Set<InsightType> insights = new HashSet<>();
    FeeClassification feeClassification;
    TransactionStatistics stats;

    public boolean hasInsight(InsightType insight) {
        return insights.contains(insight);
    }
    
    public AnalysisContextBuilder addInsight(InsightType insight) {
        return this.toBuilder().insights(new HashSet<>(this.insights) {{ add(insight); }});
    }

    public AnalysisContextBuilder addTransactionStatistics(TransactionStatistics transactionStatistics) {
        return this.toBuilder().stats(transactionStatistics);
    }
}
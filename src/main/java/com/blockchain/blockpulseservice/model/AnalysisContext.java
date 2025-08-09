package com.blockchain.blockpulseservice.model;

import lombok.Builder;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;

@Builder(toBuilder = true)
@Value
public class AnalysisContext {
    // input
    Transaction transaction;
    TransactionWindowSnapshot transactionWindowSnapshot;
    MempoolStats mempoolStats;
    //output
    @Builder.Default
    Set<PatternType> patterns = new HashSet<>();
    FeeClassification feeClassification;
    @Builder.Default
    boolean isOutlier = false;

    public AnalysisContextBuilder addInsight(PatternType insight) {
        Set<PatternType> updatedInsights = new HashSet<>(this.patterns);
        updatedInsights.add(insight);
        return this.toBuilder().patterns(updatedInsights);
    }
}
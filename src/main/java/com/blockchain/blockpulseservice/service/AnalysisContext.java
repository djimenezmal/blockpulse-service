package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.FeeClassification;
import com.blockchain.blockpulseservice.model.InsightType;
import com.blockchain.blockpulseservice.model.MempoolStats;
import com.blockchain.blockpulseservice.model.Transaction;
import lombok.Builder;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Builder(toBuilder = true)
@Value
public class AnalysisContext {
    Transaction transaction;
    TreeSet<Transaction> orderedTransactions;
    MempoolStats mempoolStats;
    @Builder.Default
    Set<InsightType> insights = new HashSet<>();
    FeeClassification feeClassification;

    public boolean hasInsight(InsightType insight) {
        return insights.contains(insight);
    }
    
    public AnalysisContextBuilder addInsight(InsightType insight) {
        return this.toBuilder().insights(new HashSet<>(this.insights) {{ add(insight); }});
    }
}
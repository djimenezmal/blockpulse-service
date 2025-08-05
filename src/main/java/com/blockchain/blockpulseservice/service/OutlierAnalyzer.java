package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.InsightType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OutlierAnalyzer extends BaseTransactionAnalyzer {
    private final TransactionStatistics stats;
    private final double outliersPercentileThreshold;

    public OutlierAnalyzer(TransactionStatistics stats,
                           @Value("${app.analysis.tx.outliers-percentile-threshold}") double outliersPercentileThreshold) {
        this.stats = stats;
        this.outliersPercentileThreshold = outliersPercentileThreshold;
    }

    @Override
    protected AnalysisContext doAnalyze(AnalysisContext context) {
        double percentile = stats.getCurrentPercentile(outliersPercentileThreshold, context.getOrderedTransactions());
        if (context.getTransaction().feePerVSize() > percentile) {
            return context
                    .addInsight(InsightType.OUTLIER)
                    .build();
        }
        return context;
    }
}
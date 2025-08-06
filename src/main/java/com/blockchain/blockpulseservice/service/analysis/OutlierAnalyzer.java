package com.blockchain.blockpulseservice.service.analysis;

import com.blockchain.blockpulseservice.model.InsightType;
import com.blockchain.blockpulseservice.utils.MathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OutlierAnalyzer extends BaseTransactionAnalyzer {
    private final double outliersPercentileThreshold;

    public OutlierAnalyzer(@Value("${app.analysis.tx.outliers-percentile-threshold:0.99}") double outliersPercentileThreshold) {
        this.outliersPercentileThreshold = outliersPercentileThreshold;
    }

    @Override
    protected AnalysisContext doAnalyze(AnalysisContext context) {
        double percentile = MathUtils.getCurrentPercentile(outliersPercentileThreshold, context.getSortedTransactionsPerFeeRate());
        if (context.getTransaction().feePerVSize() > percentile) {
            return context
                    .addInsight(InsightType.OUTLIER)
                    .build();
        }
        return context;
    }
}
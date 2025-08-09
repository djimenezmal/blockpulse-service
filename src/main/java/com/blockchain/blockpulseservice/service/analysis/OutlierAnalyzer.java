package com.blockchain.blockpulseservice.service.analysis;

import com.blockchain.blockpulseservice.model.AnalysisContext;
import org.springframework.stereotype.Component;

@Component
public class OutlierAnalyzer extends BaseTransactionAnalyzer {
    @Override
    protected AnalysisContext doAnalyze(AnalysisContext context) {
        double percentile = context.getTransactionWindowSnapshot().outlierFeeRatePercentile();
        if (context.getTransaction().feePerVSize() > percentile) {
            return context
                    .toBuilder()
                    .isOutlier(true)
                    .build();
        }
        return context;
    }
}
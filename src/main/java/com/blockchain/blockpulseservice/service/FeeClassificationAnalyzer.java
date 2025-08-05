package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.FeeClassification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeeClassificationAnalyzer extends BaseTransactionAnalyzer {
    private static final double LOCAL_CHEAP_PERCENTILE = 0.25;
    private static final double LOCAL_NORMAL_PERCENTILE = 0.75;
    private final TransactionStatistics stats;
    private final int mempoolSizeThreshold;

    public FeeClassificationAnalyzer(TransactionStatistics stats,
                                     @Value("${app.analysis.tx.mempool-congestion-vbytes-threshold}")
                                     int mempoolSizeThreshold) {
        this.stats = stats;
        this.mempoolSizeThreshold = mempoolSizeThreshold;
    }

    @Override
    protected AnalysisContext doAnalyze(AnalysisContext context) {
        FeeClassification classification = classifyFee(context);
        return context.toBuilder()
            .feeClassification(classification)
            .build();
    }
    
    private FeeClassification classifyFee(AnalysisContext context) {
        var mempoolStats = context.getMempoolStats();
        var feePerVSize = context.getTransaction().feePerVSize();
        if (mempoolStats.mempoolSize() > mempoolSizeThreshold) {
            // Network congested → use mempool recommended fees
            if (feePerVSize < mempoolStats.fastFeePerVByte()) {
                return FeeClassification.CHEAP;
            } else if (feePerVSize <= mempoolStats.mediumFeePerVByte()) {
                return FeeClassification.NORMAL;
            } else {
                return FeeClassification.EXPENSIVE;
            }
        } else {
            // Normal network → use local percentiles
            double p25 = stats.getCurrentPercentile(LOCAL_CHEAP_PERCENTILE, context.getOrderedTransactions());
            double p75 = stats.getCurrentPercentile(LOCAL_NORMAL_PERCENTILE, context.getOrderedTransactions());

            if (feePerVSize < p25) {
                return FeeClassification.CHEAP;
            } else if (feePerVSize <= p75) {
                return FeeClassification.NORMAL;
            } else {
                return FeeClassification.EXPENSIVE;
            }
        }
    }
}
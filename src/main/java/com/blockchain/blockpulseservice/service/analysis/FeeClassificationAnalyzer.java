package com.blockchain.blockpulseservice.service.analysis;

import com.blockchain.blockpulseservice.model.FeeClassification;
import com.blockchain.blockpulseservice.utils.MathUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeeClassificationAnalyzer extends BaseTransactionAnalyzer {
    private final int mempoolSizeThreshold;
    private final double localCheapPercentileThreshold;
    private final double localNormalPercentileThreshold;

    public FeeClassificationAnalyzer(@Value("${app.analysis.tx.local-cheap-percentile:0.25}")
                                     double localCheapPercentileThreshold,
                                     @Value("${app.analysis.tx.local-normal-percentile:0.75}")
                                     double localNormalPercentileThreshold,
                                     @Value("${app.analysis.tx.mempool-congestion-vbytes-threshold}")
                                     int mempoolSizeThreshold) {
        this.localCheapPercentileThreshold = localCheapPercentileThreshold;
        this.localNormalPercentileThreshold = localNormalPercentileThreshold;
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
            Percentile percentile = new Percentile();
            double localCheapPercentile = MathUtils.getCurrentPercentile(localCheapPercentileThreshold, context.getSortedTransactionsPerFeeRate());
            double localNormalPercentile = MathUtils.getCurrentPercentile(localNormalPercentileThreshold, context.getSortedTransactionsPerFeeRate());

            if (feePerVSize < localCheapPercentile) {
                return FeeClassification.CHEAP;
            } else if (feePerVSize <= localNormalPercentile) {
                return FeeClassification.NORMAL;
            } else {
                return FeeClassification.EXPENSIVE;
            }
        }
    }
}
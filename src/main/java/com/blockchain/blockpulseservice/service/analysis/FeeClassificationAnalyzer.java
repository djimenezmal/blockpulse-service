package com.blockchain.blockpulseservice.service.analysis;

import com.blockchain.blockpulseservice.model.AnalysisContext;
import com.blockchain.blockpulseservice.model.FeeClassification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeeClassificationAnalyzer extends BaseTransactionAnalyzer {
    private final int mempoolSizeThreshold;

    public FeeClassificationAnalyzer(@Value("${app.analysis.tx.mempool-congestion-vbytes-threshold}")
                                     int mempoolSizeThreshold) {
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
            double firstQuartile = context.getTransactionWindowSnapshot().firstQuartile();
            double thirdQuartile = context.getTransactionWindowSnapshot().thirdQuartile();

            if (feePerVSize < firstQuartile) {
                return FeeClassification.CHEAP;
            } else if (feePerVSize <= thirdQuartile) {
                return FeeClassification.NORMAL;
            } else {
                return FeeClassification.EXPENSIVE;
            }
        }
    }
}
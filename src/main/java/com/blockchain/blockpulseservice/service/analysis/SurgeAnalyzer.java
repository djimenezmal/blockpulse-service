package com.blockchain.blockpulseservice.service.analysis;

import com.blockchain.blockpulseservice.model.InsightType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SurgeAnalyzer extends BaseTransactionAnalyzer {
    private final double mempoolThreshold;

    public SurgeAnalyzer(@Value("${app.analysis.tx.mempool-congestion-vbytes-threshold}") double mempoolThreshold) {
        this.mempoolThreshold = mempoolThreshold;
    }

    @Override
    protected AnalysisContext doAnalyze(AnalysisContext context) {
        var mempoolStats = context.getMempoolStats();
        var transaction = context.getTransaction();
        boolean isSurge = context.hasInsight(InsightType.OUTLIER) &&
                transaction.feePerVSize() > mempoolStats.fastFeePerVByte() &&
                mempoolStats.mempoolSize() >= mempoolThreshold;
        if (isSurge) {
            log.info("Surge detected for tx: {}", context.getTransaction().hash());
            return context
                    .addInsight(InsightType.SURGE)
                    .build();
        }
        return context;
    }
}
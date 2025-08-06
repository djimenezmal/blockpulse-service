package com.blockchain.blockpulseservice.config.analysis;

import com.blockchain.blockpulseservice.service.analysis.FeeClassificationAnalyzer;
import com.blockchain.blockpulseservice.service.analysis.OutlierAnalyzer;
import com.blockchain.blockpulseservice.service.analysis.SurgeAnalyzer;
import com.blockchain.blockpulseservice.service.analysis.TransactionAnalyzer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalysisChainConfig {

    @Bean
    public TransactionAnalyzer analysisChain(OutlierAnalyzer outlierAnalyzer,
                                             SurgeAnalyzer surgeAnalyzer,
                                             FeeClassificationAnalyzer feeClassificationAnalyzer) {
        outlierAnalyzer
                .setNext(surgeAnalyzer)
                .setNext(feeClassificationAnalyzer);

        return outlierAnalyzer;
    }
}
package com.blockchain.blockpulseservice.config.analysis;

import com.blockchain.blockpulseservice.service.FeeClassificationAnalyzer;
import com.blockchain.blockpulseservice.service.OutlierAnalyzer;
import com.blockchain.blockpulseservice.service.SurgeAnalyzer;
import com.blockchain.blockpulseservice.service.TransactionAnalyzer;
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
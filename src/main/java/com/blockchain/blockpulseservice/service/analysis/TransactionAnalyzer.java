package com.blockchain.blockpulseservice.service.analysis;

import com.blockchain.blockpulseservice.model.AnalysisContext;

public interface TransactionAnalyzer {
    AnalysisContext analyze(AnalysisContext context);
    TransactionAnalyzer setNext(TransactionAnalyzer next);
}
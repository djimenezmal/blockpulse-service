package com.blockchain.blockpulseservice.service.analysis;

public interface TransactionAnalyzer {
    AnalysisContext analyze(AnalysisContext context);
    TransactionAnalyzer setNext(TransactionAnalyzer next);
}
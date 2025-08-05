package com.blockchain.blockpulseservice.service;

public interface TransactionAnalyzer {
    AnalysisContext analyze(AnalysisContext context);
    TransactionAnalyzer setNext(TransactionAnalyzer next);
}
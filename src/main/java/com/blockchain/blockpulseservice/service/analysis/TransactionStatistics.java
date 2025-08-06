package com.blockchain.blockpulseservice.service.analysis;

public record TransactionStatistics(double averageFeeRatePerVSize,
                                    double maxFeeRatePerVSize,
                                    double minFeeRatePerVSize,
                                    int totalAnalyzedTransactions) {
}


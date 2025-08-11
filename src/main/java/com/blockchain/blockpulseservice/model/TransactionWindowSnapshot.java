package com.blockchain.blockpulseservice.model;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;

public record TransactionWindowSnapshot(int transactionsCount,
                                        BigDecimal avgFeePerVByte,
                                        BigDecimal medianFeePerVByte,
                                        int outliersCount,
                                        BigDecimal outlierFeePerVBytePercentile,
                                        BigDecimal firstQuartile,
                                        BigDecimal thirdQuartile,
                                        List<Transaction> transactions) {
    public static TransactionWindowSnapshot empty() {
        return new TransactionWindowSnapshot(0, ZERO, ZERO, 0, ZERO,  ZERO,ZERO, emptyList());
    }
}
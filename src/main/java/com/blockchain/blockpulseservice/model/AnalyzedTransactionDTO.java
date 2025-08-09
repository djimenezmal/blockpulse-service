package com.blockchain.blockpulseservice.model;

import lombok.Builder;

import java.util.Set;

@Builder
public record AnalyzedTransactionDTO(String id,
                                     double feePerVByte,
                                     double totalFee,
                                     int size,
                                     long timestamp,
                                     Set<PatternType> patterns,
                                     FeeClassification feeClassification,
                                     boolean isOutlier,
                                     TransactionWindowSnapshotDTO windowSnapshotDTO) {
}
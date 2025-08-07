package com.blockchain.blockpulseservice.model;

import com.blockchain.blockpulseservice.service.sliding_window.TransactionWindowSnapshotDTO;
import lombok.Builder;

import java.util.Set;

@Builder
public record AnalyzedTransactionDTO(String id,
                                     double feePerVSize,
                                     double totalFee,
                                     int size,
                                     long time,
                                     Set<InsightType> insights,
                                     FeeClassification feeClassification,
                                     boolean isOutlier,
                                     TransactionWindowSnapshotDTO windowSnapshotDTO) {
}
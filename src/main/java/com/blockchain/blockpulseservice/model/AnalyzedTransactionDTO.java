package com.blockchain.blockpulseservice.model;

import java.util.Set;

public record AnalyzedTransactionDTO(String id,
                                     double feePerVSize,
                                     double totalFee,
                                     int size,
                                     long time,
                                     Set<InsightType> insightType,
                                     FeeClassification feeClassification,
                                     boolean isOutlier) {}
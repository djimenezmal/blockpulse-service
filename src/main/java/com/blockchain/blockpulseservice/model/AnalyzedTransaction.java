package com.blockchain.blockpulseservice.model;

import java.util.List;

public record AnalyzedTransaction(String id,
                                  double feePerVSize,
                                  double totalFee,
                                  int size,
                                  long time,
                                  List<InsightType> insightType,
                                  FeeClassification feeClassification) {}
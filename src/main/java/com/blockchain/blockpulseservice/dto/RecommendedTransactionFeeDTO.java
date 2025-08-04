package com.blockchain.blockpulseservice.dto;

public record RecommendedTransactionFeeDTO(double fastestFee,
                                           double halfHourFee,
                                           double hourFee,
                                           double economyFee
) {}
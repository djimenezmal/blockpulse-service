package com.blockchain.blockpulseservice.dto;

public record RecommendedTransactionFeeDTO(double fastFee, //tx goes to next block
                                           double mediumFee, //tx goes within 3 blocks
                                           double slowFee //tx goes within 6 blocks
) {}
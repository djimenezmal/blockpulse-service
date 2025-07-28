package com.blockchain.blockpulseservice.tx;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public class TransactionInput {
    private String previousTransactionHash;
    private Integer previousOutputIndex;
    private String scriptSignature;
    private String address;
    private BigDecimal value;
    private Long sequence;
}
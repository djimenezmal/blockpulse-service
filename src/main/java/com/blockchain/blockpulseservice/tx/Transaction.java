package com.blockchain.blockpulseservice.tx;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class Transaction {
    private String hash;
    private Long blockHeight;
    private Instant timestamp;
    private BigDecimal totalValue;
    private BigDecimal fee;
    private Integer inputCount;
    private Integer outputCount;
    private List<TransactionInput> inputs;
    private List<TransactionOutput> outputs;
    private Integer size;
    private Integer virtualSize;
    private Boolean confirmed;
}
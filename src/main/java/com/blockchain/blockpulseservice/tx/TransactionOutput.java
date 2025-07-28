package com.blockchain.blockpulseservice.tx;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionOutput {
    private Integer index;
    private BigDecimal value;
    private String scriptPubKey;
    private String address;
    private Boolean spent;
}
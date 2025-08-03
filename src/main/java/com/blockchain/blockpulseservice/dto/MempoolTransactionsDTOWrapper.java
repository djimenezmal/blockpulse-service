package com.blockchain.blockpulseservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MempoolTransactionsDTOWrapper(@JsonProperty("mempool-transactions") MempoolTransactionsDTO mempoolTransactions) {
    public record MempoolTransactionsDTO(List<TransactionDTO> added) {}
    public record TransactionDTO(@JsonProperty("txid") String id,
                                 @JsonProperty("size") int size,
                                 @JsonProperty("vsize") int vSize,
                                 @JsonProperty("totalFee") int fee,
                                 @JsonProperty("feePerVsize") double feePerVSize,
                                 @JsonProperty("firstSeen") long firstSeen
    ) {
    }
}
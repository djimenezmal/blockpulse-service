package com.blockchain.blockpulseservice.tx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockchainInfoWebSocketMessage {
    @JsonProperty("op")
    private String operation;
    
    @JsonProperty("x")
    private BlockchainInfoTransactionDto transaction;
}
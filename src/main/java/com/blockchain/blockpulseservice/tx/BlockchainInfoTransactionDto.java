package com.blockchain.blockpulseservice.tx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockchainInfoTransactionDto {
    @JsonProperty("hash")
    private String hash;
    
    @JsonProperty("block_height")
    private Long blockHeight;
    
    @JsonProperty("time")
    private Long time;
    
    @JsonProperty("size")
    private Integer size;
    
    @JsonProperty("vsize")
    private Integer virtualSize;
    
    @JsonProperty("fee")
    private Long fee;
    
    @JsonProperty("inputs")
    private List<BlockchainInfoInputDto> inputs;
    
    @JsonProperty("out")
    private List<BlockchainInfoOutputDto> outputs;
}
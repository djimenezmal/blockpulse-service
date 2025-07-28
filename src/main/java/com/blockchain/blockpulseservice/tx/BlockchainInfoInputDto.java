package com.blockchain.blockpulseservice.tx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockchainInfoInputDto {
    @JsonProperty("prev_out")
    private BlockchainInfoPrevOutDto prevOut;
    
    @JsonProperty("script")
    private String script;
    
    @JsonProperty("sequence")
    private Long sequence;
}
package com.blockchain.blockpulseservice.tx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockchainInfoPrevOutDto {
    @JsonProperty("hash")
    private String hash;
    
    @JsonProperty("n")
    private Integer n;
    
    @JsonProperty("addr")
    private String address;
    
    @JsonProperty("value")
    private Long value;
}
package com.blockchain.blockpulseservice.tx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockchainInfoOutputDto {
    @JsonProperty("n")
    private Integer n;
    
    @JsonProperty("value")
    private Long value;
    
    @JsonProperty("script")
    private String script;
    
    @JsonProperty("addr")
    private String address;
    
    @JsonProperty("spent")
    private Boolean spent;
}
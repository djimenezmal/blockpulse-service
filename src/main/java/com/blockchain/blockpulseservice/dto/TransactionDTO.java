package com.blockchain.blockpulseservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TransactionDTO(
        @JsonProperty("size") int size,
        @JsonProperty("hash") String hash,
        @JsonProperty("time") long time,
        @JsonProperty("inputs") List<InputDTO> inputs,
        @JsonProperty("out") List<OutputDTO> outputs) {}
record InputDTO(@JsonProperty("prev_out") PrevOutDTO prevOut) {}
record PrevOutDTO(@JsonProperty("value") long value) {}
record OutputDTO (@JsonProperty("value") long value){}
package com.blockchain.blockpulseservice.tx;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TransactionData(
        @JsonProperty("size") int size,
        @JsonProperty("hash") String hash,
        @JsonProperty("inputs") List<Input> inputs,
        @JsonProperty("out") List<Output> outputs) {}
record Input(@JsonProperty("prev_out") PrevOut prevOut) {}
record PrevOut(@JsonProperty("value") long value) {}
package com.blockchain.blockpulseservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MempoolInfoDTO(@JsonProperty("count") int memPoolSize) { }
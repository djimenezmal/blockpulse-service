package com.blockchain.blockpulseservice.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MempoolInfoDTO(@JsonProperty("count") int memPoolSize) {
}

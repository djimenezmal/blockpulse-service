package com.blockchain.blockpulseservice.tx;

public record TransactionDTO(long fee, int size, String hash) {}
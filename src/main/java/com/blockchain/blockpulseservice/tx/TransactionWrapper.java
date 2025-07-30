package com.blockchain.blockpulseservice.tx;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransactionWrapper(@JsonProperty("x") TransactionData transactionData) {}
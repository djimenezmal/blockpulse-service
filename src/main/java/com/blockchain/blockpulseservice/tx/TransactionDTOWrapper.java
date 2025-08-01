package com.blockchain.blockpulseservice.tx;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransactionDTOWrapper(@JsonProperty("x") TransactionDTO transactionDTO) {}
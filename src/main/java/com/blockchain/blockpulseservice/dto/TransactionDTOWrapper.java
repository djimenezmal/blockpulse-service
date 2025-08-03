package com.blockchain.blockpulseservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransactionDTOWrapper(@JsonProperty("x") TransactionDTO transactionDTO) {}
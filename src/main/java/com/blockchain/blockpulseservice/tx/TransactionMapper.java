package com.blockchain.blockpulseservice.tx;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.ToLongFunction;

@Component
public class TransactionMapper {
    public TransactionDTO mapToTransactionDTO(TransactionData txData) {
        var totalInput = sum(txData.inputs(), input -> input.prevOut().value());
        var totalOutput = sum(txData.outputs(), Output::value);
        var fee = totalInput - totalOutput;

        return new TransactionDTO(fee, txData.size(), txData.hash());
    }

    private <T> long sum(List<T> list, ToLongFunction<T> toLongFunction) {
        return list.stream()
                .mapToLong(toLongFunction)
                .sum();
    }
}
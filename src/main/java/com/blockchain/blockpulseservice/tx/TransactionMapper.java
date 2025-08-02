package com.blockchain.blockpulseservice.tx;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.ToLongFunction;

@Component
public class TransactionMapper {
    public Transaction mapToTransaction(TransactionDTO txData) {
        var totalInput = sum(txData.inputs(), input -> input.prevOut().value());
        var totalOutput = sum(txData.outputs(), Output::value);
        var fee = totalInput - totalOutput;

        return new Transaction(txData.hash(), fee, txData.size(), txData.time());
    }

    private <T> long sum(List<T> txs, ToLongFunction<T> toLongFunction) {
        return txs.stream()
                .mapToLong(toLongFunction)
                .sum();
    }
}
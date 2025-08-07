package com.blockchain.blockpulseservice.service.sliding_window;

import com.blockchain.blockpulseservice.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionsPercentile {
    public double getPercentileFeeRate(double percentile, List<Transaction> transactions) {
        int index = getPercentileIndex(percentile, transactions.size());
        return transactions.get(Math.max(0, index)).feePerVSize();
    }

    public int getPercentileIndex(double percentile, int totalTransactions) {
        return (int) Math.ceil(percentile * totalTransactions) - 1;
    }
}

package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.Transaction;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.TreeSet;

@Component
public class TransactionStatistics {
    @Getter
    private double averageFeeRate;
    private double maxFeeRate;
    private double minFeeRate;

    protected double getCurrentPercentile(double percentile, TreeSet<Transaction> orderedTxsByFee) {
        int index = (int) Math.ceil(percentile * orderedTxsByFee.size()) - 1;
        var sortedArray = orderedTxsByFee.toArray(new Transaction[0]);
        return sortedArray[index].totalFee();
    }
}

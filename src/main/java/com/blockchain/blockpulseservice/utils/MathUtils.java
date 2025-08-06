package com.blockchain.blockpulseservice.utils;

import com.blockchain.blockpulseservice.model.Transaction;

public class MathUtils {
    public static double getCurrentPercentile(double percentile, Transaction[] sortedTransactions) {
        int index = (int) Math.ceil(percentile * sortedTransactions.length) - 1;
        return sortedTransactions[index].totalFee();
    }
}

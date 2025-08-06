package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.TreeSet;

@Service
class WindowSnapshotService {
    public WindowSnapshot takeCurrentWindowSnapshot(TreeSet<Transaction> orderedTransactionsPerFeeRate) {
        if (orderedTransactionsPerFeeRate.isEmpty()) {
            return WindowSnapshot.empty();
        }

        int totalTransactions = orderedTransactionsPerFeeRate.size();
        double sum = 0;
        var transactions = new ArrayList<Transaction>();
        for (Transaction transaction : orderedTransactionsPerFeeRate) {
            sum += transaction.feePerVSize();
            transactions.add(transaction);
        }
        double averageFeeRate = sum/totalTransactions;

        return new WindowSnapshot(
                totalTransactions,
                averageFeeRate,
                getMedianFeeRate(transactions),
                transactions
        );
    }

    public double getMedianFeeRate(ArrayList<Transaction> transactions) {
        int size = transactions.size();
        if (size % 2 == 0) {
            return (transactions.get(size / 2 - 1).feePerVSize() +
                    transactions.get(size / 2).feePerVSize()) / 2.0;
        } else {
            return transactions.get(size / 2).feePerVSize();
        }
    }
}
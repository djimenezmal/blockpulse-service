package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Slf4j
@Service
class TransactionWindowSnapshotService {
    public TransactionWindowSnapshot takeCurrentWindowSnapshot(TreeSet<Transaction> orderedTransactionsPerFeeRate) {
        log.debug("Taking current window snapshot...");
        if (orderedTransactionsPerFeeRate.isEmpty()) {
            return TransactionWindowSnapshot.empty();
        }

        int totalTransactions = orderedTransactionsPerFeeRate.size();
        double sum = 0;
        var transactions = new ArrayList<Transaction>();
        for (Transaction transaction : orderedTransactionsPerFeeRate) {
            sum += transaction.feePerVSize();
            transactions.add(transaction);
        }
        double averageFeeRate = sum/totalTransactions;

        return new TransactionWindowSnapshot(
                totalTransactions,
                averageFeeRate,
                getMedianFeeRate(transactions),
                transactions
        );
    }

    public double getMedianFeeRate(List<Transaction> transactions) {
        int size = transactions.size();
        if (size % 2 == 0) {
            return (transactions.get(size / 2 - 1).feePerVSize() +
                    transactions.get(size / 2).feePerVSize()) / 2.0;
        } else {
            return transactions.get(size / 2).feePerVSize();
        }
    }
}
package com.blockchain.blockpulseservice.service.sliding_window;

public record TransactionWindowSnapshotDTO(
        int totalTransactions,
        double averageFeeRatePerVSize,
        double medianFeeRatePerVSize,
        int numOfOutliers) {
}
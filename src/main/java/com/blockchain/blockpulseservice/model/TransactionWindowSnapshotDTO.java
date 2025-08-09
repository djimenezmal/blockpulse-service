package com.blockchain.blockpulseservice.model;

public record TransactionWindowSnapshotDTO(
        int totalTransactions,
        double averageFeeRatePerVByte,
        double medianFeeRatePerVByte,
        int numOfOutliers) {
}
package com.blockchain.blockpulseservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeAnalysisService {
    

    public FeeAnalysisDTO generateFeeAnalysis(Transaction.CryptoCurrency currency) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        // Get recent transactions
        List<Transaction> recentTxs = transactionRepository.findRecentTransactions(currency, oneHourAgo);
        List<Transaction> outliers = transactionRepository.findRecentOutliers(currency, oneHourAgo);
        List<Transaction> spamTxs = transactionRepository.findRecentSpamTransactions(currency, oneHourAgo);
        List<BlockStats> recentBlockStats = blockStatsRepository.findRecentBlockStats(currency, oneHourAgo);
        
        if (recentTxs.isEmpty()) {
            return createEmptyAnalysis(currency);
        }
        
        // Calculate fee statistics
        FeeAnalysisDTO.FeeStatistics feeStats = calculateFeeStatistics(recentTxs);
        
        // Process outliers
        List<FeeAnalysisDTO.OutlierTransaction> outlierDTOs = outliers.stream()
            .map(this::convertToOutlierDTO)
            .collect(Collectors.toList());
        
        // Process spam transactions
        List<FeeAnalysisDTO.SpamTransaction> spamDTOs = spamTxs.stream()
            .map(this::convertToSpamDTO)
            .collect(Collectors.toList());
        
        // Analyze latest block
        FeeAnalysisDTO.BlockAnalysis blockAnalysis = analyzeLatestBlock(recentBlockStats);
        
        return FeeAnalysisDTO.builder()
            .currency(currency)
            .timestamp(LocalDateTime.now())
            .feeStats(feeStats)
            .outliers(outlierDTOs)
            .spamTransactions(spamDTOs)
            .blockAnalysis(blockAnalysis)
            .build();
    }
    
    private FeeAnalysisDTO.FeeStatistics calculateFeeStatistics(List<Transaction> transactions) {
        List<BigDecimal> fees = transactions.stream()
            .map(Transaction::getFeePerByte)
            .sorted()
            .collect(Collectors.toList());
        
        BigDecimal sum = fees.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(fees.size()), 8, RoundingMode.HALF_UP);
        
        BigDecimal median = calculatePercentile(fees, 50);
        BigDecimal min = fees.get(0);
        BigDecimal max = fees.get(fees.size() - 1);
        BigDecimal p25 = calculatePercentile(fees, 25);
        BigDecimal p75 = calculatePercentile(fees, 75);
        BigDecimal p95 = calculatePercentile(fees, 95);
        
        // Calculate standard deviation
        BigDecimal variance = fees.stream()
            .map(fee -> fee.subtract(avg).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(fees.size()), 8, RoundingMode.HALF_UP);
        
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        
        return FeeAnalysisDTO.FeeStatistics.builder()
            .avgFeePerByte(avg)
            .medianFeePerByte(median)
            .minFeePerByte(min)
            .maxFeePerByte(max)
            .percentile25(p25)
            .percentile75(p75)
            .percentile95(p95)
            .standardDeviation(stdDev)
            .build();
    }
    
    private BigDecimal calculatePercentile(List<BigDecimal> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        double index = (percentile / 100.0) * (sortedValues.size() - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        
        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        }
        
        BigDecimal lowerValue = sortedValues.get(lowerIndex);
        BigDecimal upperValue = sortedValues.get(upperIndex);
        BigDecimal weight = BigDecimal.valueOf(index - lowerIndex);
        
        return lowerValue.add(upperValue.subtract(lowerValue).multiply(weight));
    }
    
    private FeeAnalysisDTO.OutlierTransaction convertToOutlierDTO(Transaction tx) {
        // Get recent transactions for comparison
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Transaction> recentTxs = transactionRepository.findRecentTransactions(tx.getCurrency(), oneHourAgo);
        
        List<BigDecimal> fees = recentTxs.stream()
            .map(Transaction::getFeePerByte)
            .sorted()
            .collect(Collectors.toList());
        
        BigDecimal median = calculatePercentile(fees, 50);
        BigDecimal deviation = tx.getFeePerByte().subtract(median);
        
        String outlierType = deviation.compareTo(BigDecimal.ZERO) > 0 ? "HIGH" : "LOW";
        
        return FeeAnalysisDTO.OutlierTransaction.builder()
            .txHash(tx.getTxHash())
            .feePerByte(tx.getFeePerByte())
            .deviationFromMedian(deviation.abs())
            .outlierType(outlierType)
            .timestamp(tx.getTimestamp())
            .build();
    }
    
    private FeeAnalysisDTO.SpamTransaction convertToSpamDTO(Transaction tx) {
        String spamReason = determineSpamReason(tx);
        
        return FeeAnalysisDTO.SpamTransaction.builder()
            .txHash(tx.getTxHash())
            .feePerByte(tx.getFeePerByte())
            .transactionSize(tx.getTransactionSize())
            .spamReason(spamReason)
            .timestamp(tx.getTimestamp())
            .build();
    }
    
    private String determineSpamReason(Transaction tx) {
        if (tx.getTransactionSize() < 250) {
            return "SMALL_SIZE_HIGH_FEE";
        }
        // Add more spam detection logic here
        return "SUSPICIOUS_PATTERN";
    }
    
    private FeeAnalysisDTO.BlockAnalysis analyzeLatestBlock(List<BlockStats> recentBlockStats) {
        if (recentBlockStats.isEmpty()) {
            return createEmptyBlockAnalysis();
        }
        
        BlockStats latestBlock = recentBlockStats.get(0);
        
        // Determine block utilization
        long maxSize = latestBlock.getCurrency() == Transaction.CryptoCurrency.BTC ? 1_000_000 : 32_000_000;
        BigDecimal utilization = BigDecimal.valueOf(latestBlock.getBlockSize())
            .divide(BigDecimal.valueOf(maxSize), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        // Determine pattern
        String pattern = determineBlockPattern(latestBlock, recentBlockStats);
        
        return FeeAnalysisDTO.BlockAnalysis.builder()
            .blockHeight(latestBlock.getBlockHeight())
            .isFull(latestBlock.getIsFull())
            .hasLowFees(latestBlock.getAvgFeePerByte().compareTo(BigDecimal.valueOf(10)) < 0)
            .hasFeeWar(latestBlock.getHasFeeWar())
            .transactionCount(latestBlock.getTransactionCount())
            .blockSize(latestBlock.getBlockSize())
            .utilizationPercentage(utilization)
            .pattern(pattern)
            .build();
    }
    
    private String determineBlockPattern(BlockStats currentBlock, List<BlockStats> recentBlocks) {
        if (currentBlock.getHasFeeWar()) {
            return "FEE_WAR";
        }
        
        if (currentBlock.getIsFull()) {
            // Check if multiple recent blocks are full
            long fullBlocks = recentBlocks.stream()
                .limit(5)
                .mapToLong(b -> b.getIsFull() ? 1 : 0)
                .sum();
            
            if (fullBlocks >= 3) {
                return "CONGESTION";
            }
        }
        
        // Check for spam attack pattern
        if (currentBlock.getTransactionCount() > 2000 && 
            currentBlock.getAvgFeePerByte().compareTo(BigDecimal.valueOf(5)) < 0) {
            return "SPAM_ATTACK";
        }
        
        return "NORMAL";
    }
    
    private FeeAnalysisDTO createEmptyAnalysis(Transaction.CryptoCurrency currency) {
        return FeeAnalysisDTO.builder()
            .currency(currency)
            .timestamp(LocalDateTime.now())
            .feeStats(FeeAnalysisDTO.FeeStatistics.builder()
                .avgFeePerByte(BigDecimal.ZERO)
                .medianFeePerByte(BigDecimal.ZERO)
                .minFeePerByte(BigDecimal.ZERO)
                .maxFeePerByte(BigDecimal.ZERO)
                .percentile25(BigDecimal.ZERO)
                .percentile75(BigDecimal.ZERO)
                .percentile95(BigDecimal.ZERO)
                .standardDeviation(BigDecimal.ZERO)
                .build())
            .outliers(List.of())
            .spamTransactions(List.of())
            .blockAnalysis(createEmptyBlockAnalysis())
            .build();
    }
    
    private FeeAnalysisDTO.BlockAnalysis createEmptyBlockAnalysis() {
        return FeeAnalysisDTO.BlockAnalysis.builder()
            .blockHeight(0L)
            .isFull(false)
            .hasLowFees(false)
            .hasFeeWar(false)
            .transactionCount(0)
            .blockSize(0L)
            .utilizationPercentage(BigDecimal.ZERO)
            .pattern("NO_DATA")
            .build();
    }
}
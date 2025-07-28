package com.blockchain.blockpulseservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final BlockStatsRepository blockStatsRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FeeAnalysisService feeAnalysisService;
    
    @Transactional
    public void processNewTransaction(Transaction transaction) {
        try {
            // Save transaction
            transactionRepository.save(transaction);
            
            // Analyze transaction for outliers and spam
            analyzeTransaction(transaction);
            
            // Send real-time update
            sendTransactionUpdate(transaction);
            
            log.debug("Processed new {} transaction: {}", transaction.getCurrency(), transaction.getTxHash());
            
        } catch (Exception e) {
            log.error("Error processing transaction: {}", transaction.getTxHash(), e);
        }
    }
    
    @Transactional
    public void updateConfirmedTransactions(Transaction.CryptoCurrency currency, Long blockHeight, LocalDateTime confirmedAt) {
        try {
            // Find unconfirmed transactions and update them
            List<Transaction> unconfirmedTxs = transactionRepository.findByCurrencyAndBlockHeight(currency, 0L);
            
            for (Transaction tx : unconfirmedTxs) {
                tx.setBlockHeight(blockHeight);
                tx.setConfirmedAt(confirmedAt);
                transactionRepository.save(tx);
            }
            
            // Analyze block statistics
            analyzeBlockStats(currency, blockHeight);
            
            log.info("Updated {} confirmed transactions for block {}", unconfirmedTxs.size(), blockHeight);
            
        } catch (Exception e) {
            log.error("Error updating confirmed transactions for block {}", blockHeight, e);
        }
    }
    
    private void analyzeTransaction(Transaction transaction) {
        // Get recent transactions for comparison
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Transaction> recentTxs = transactionRepository.findRecentTransactions(transaction.getCurrency(), oneHourAgo);
        
        if (recentTxs.size() < 10) {
            return; // Not enough data for analysis
        }
        
        // Calculate statistics
        BigDecimal medianFee = calculateMedian(recentTxs.stream()
            .map(Transaction::getFeePerByte)
            .collect(Collectors.toList()));
        
        BigDecimal avgFee = recentTxs.stream()
            .map(Transaction::getFeePerByte)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(recentTxs.size()), 8, RoundingMode.HALF_UP);
        
        // Detect outliers (transactions with fees > 3 standard deviations from median)
        BigDecimal stdDev = calculateStandardDeviation(recentTxs, avgFee);
        BigDecimal threshold = medianFee.add(stdDev.multiply(BigDecimal.valueOf(3)));
        
        if (transaction.getFeePerByte().compareTo(threshold) > 0) {
            transaction.setIsOutlier(true);
            log.info("Outlier detected: {} - Fee: {} sat/byte (Median: {})", 
                transaction.getTxHash(), transaction.getFeePerByte(), medianFee);
        }
        
        // Detect spam (very small transactions with disproportionately high fees)
        if (transaction.getTransactionSize() < 250 && 
            transaction.getFeePerByte().compareTo(medianFee.multiply(BigDecimal.valueOf(5))) > 0) {
            transaction.setIsSpam(true);
            log.info("Spam transaction detected: {} - Size: {} bytes, Fee: {} sat/byte", 
                transaction.getTxHash(), transaction.getTransactionSize(), transaction.getFeePerByte());
        }
        
        transactionRepository.save(transaction);
    }
    
    private void analyzeBlockStats(Transaction.CryptoCurrency currency, Long blockHeight) {
        List<Transaction> blockTxs = transactionRepository.findByCurrencyAndBlockHeight(currency, blockHeight);
        
        if (blockTxs.isEmpty()) {
            return;
        }
        
        // Calculate block statistics
        long blockSize = blockTxs.stream().mapToLong(tx -> tx.getTransactionSize()).sum();
        BigDecimal avgFee = blockTxs.stream()
            .map(Transaction::getFeePerByte)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(blockTxs.size()), 8, RoundingMode.HALF_UP);
        
        List<BigDecimal> fees = blockTxs.stream()
            .map(Transaction::getFeePerByte)
            .sorted()
            .collect(Collectors.toList());
        
        BigDecimal medianFee = calculateMedian(fees);
        BigDecimal minFee = fees.get(0);
        BigDecimal maxFee = fees.get(fees.size() - 1);
        
        // Determine if block is full (assuming 1MB for BTC, 32MB for Kaspa)
        long maxBlockSize = currency == Transaction.CryptoCurrency.BTC ? 1_000_000 : 32_000_000;
        boolean isFull = blockSize > (maxBlockSize * 0.95); // 95% full threshold
        
        // Detect fee war (high variance in fees within the same block)
        BigDecimal feeRange = maxFee.subtract(minFee);
        boolean hasFeeWar = feeRange.compareTo(medianFee.multiply(BigDecimal.valueOf(2))) > 0;
        
        BlockStats blockStats = BlockStats.builder()
            .blockHeight(blockHeight)
            .currency(currency)
            .transactionCount(blockTxs.size())
            .blockSize(blockSize)
            .avgFeePerByte(avgFee)
            .medianFeePerByte(medianFee)
            .minFeePerByte(minFee)
            .maxFeePerByte(maxFee)
            .timestamp(LocalDateTime.now())
            .isFull(isFull)
            .hasFeeWar(hasFeeWar)
            .build();
        
        blockStatsRepository.save(blockStats);
        
        // Send block analysis update
        sendBlockAnalysisUpdate(blockStats);
        
        log.info("Block {} analysis complete - Txs: {}, Size: {} bytes, Avg Fee: {} sat/byte", 
            blockHeight, blockTxs.size(), blockSize, avgFee);
    }
    
    private BigDecimal calculateMedian(List<BigDecimal> values) {
        List<BigDecimal> sorted = values.stream().sorted().collect(Collectors.toList());
        int size = sorted.size();
        if (size % 2 == 0) {
            return sorted.get(size / 2 - 1).add(sorted.get(size / 2))
                .divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
        } else {
            return sorted.get(size / 2);
        }
    }
    
    private BigDecimal calculateStandardDeviation(List<Transaction> transactions, BigDecimal mean) {
        BigDecimal sum = transactions.stream()
            .map(tx -> tx.getFeePerByte().subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal variance = sum.divide(BigDecimal.valueOf(transactions.size()), 8, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }
    
    private void sendTransactionUpdate(Transaction transaction) {
        messagingTemplate.convertAndSend("/topic/transactions/" + transaction.getCurrency().name().toLowerCase(), 
            transaction);
    }
    
    private void sendBlockAnalysisUpdate(BlockStats blockStats) {
        messagingTemplate.convertAndSend("/topic/blocks/" + blockStats.getCurrency().name().toLowerCase(), 
            blockStats);
    }
    
    public FeeAnalysisDTO getCurrentFeeAnalysis(Transaction.CryptoCurrency currency) {
        return feeAnalysisService.generateFeeAnalysis(currency);
    }
}
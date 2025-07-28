package com.blockchain.blockpulseservice;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeeAnalysisDTO {
    
    private Transaction.CryptoCurrency currency;
    private LocalDateTime timestamp;
    private FeeStatistics feeStats;
    private List<OutlierTransaction> outliers;
    private List<SpamTransaction> spamTransactions;
    private BlockAnalysis blockAnalysis;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeeStatistics {
        private BigDecimal avgFeePerByte;
        private BigDecimal medianFeePerByte;
        private BigDecimal minFeePerByte;
        private BigDecimal maxFeePerByte;
        private BigDecimal percentile25;
        private BigDecimal percentile75;
        private BigDecimal percentile95;
        private BigDecimal standardDeviation;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OutlierTransaction {
        private String txHash;
        private BigDecimal feePerByte;
        private BigDecimal deviationFromMedian;
        private String outlierType; // "HIGH", "LOW"
        private LocalDateTime timestamp;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SpamTransaction {
        private String txHash;
        private BigDecimal feePerByte;
        private Integer transactionSize;
        private String spamReason;
        private LocalDateTime timestamp;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BlockAnalysis {
        private Long blockHeight;
        private Boolean isFull;
        private Boolean hasLowFees;
        private Boolean hasFeeWar;
        private Integer transactionCount;
        private Long blockSize;
        private BigDecimal utilizationPercentage;
        private String pattern; // "NORMAL", "CONGESTION", "FEE_WAR", "SPAM_ATTACK"
    }
}
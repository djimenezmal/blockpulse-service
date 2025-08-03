package com.blockchain.blockpulseservice.service;

/**
 * Service for analyzing Bitcoin transaction fees and detecting patterns/outliers
 */
public class TransactionFeeAnalysisService {
/**
    // Configuration constants
    private static final int ROLLING_WINDOW_SIZE = 1000; // Number of transactions to keep in memory
    private static final double OUTLIER_THRESHOLD = 2.5; // Standard deviations for outlier detection
    private static final int MIN_TRANSACTIONS_FOR_ANALYSIS = 50;
    private static final double HIGH_FEE_MULTIPLIER = 3.0; // 3x median = high totalFee
    private static final double SPAM_FEE_THRESHOLD = 1.0; // Very low fees might indicate spam

    // Rolling window of transactions
    private final Queue<TransactionData> transactionWindow = new ConcurrentLinkedQueue<>();
    private final Queue<BlockData> blockWindow = new ConcurrentLinkedQueue<>();

    // Statistics tracking
    private volatile FeeStatistics currentStats = new FeeStatistics();
    private volatile long lastAnalysisTime = 0;

    public AnalysisResult processTransaction(TransactionData transaction) {
        // Add to rolling window
        transactionWindow.offer(transaction);

        // Maintain window size
        while (transactionWindow.size() > ROLLING_WINDOW_SIZE) {
            transactionWindow.poll();
        }

        // Update statistics
        updateStatistics();

        // Perform analysis
        return analyzeTransaction(transaction);
    }

//    public void processBlock(BlockData block) {
//        blockWindow.offer(block);
//
//        // Keep last 10 blocks for pattern analysis
//        while (blockWindow.size() > 10) {
//            blockWindow.poll();
//        }
//
//        analyzeBlockPatterns();
//    }

    private AnalysisResult analyzeTransaction(TransactionData transaction) {
        AnalysisResult result = new AnalysisResult(transaction);

        if (transactionWindow.size() < MIN_TRANSACTIONS_FOR_ANALYSIS) {
            result.addInsight("Insufficient data for analysis", Insight.InsightType.INFO);
            return result;
        }

        double feePerByte = transaction.getFeePerByte();

        // Outlier detection using statistical methods
        detectFeeOutliers(transaction, result);

        // Spam detection
        detectSpamTransactions(transaction, result);

        // Fee surge detection
        detectFeeSurges(transaction, result);

        return result;
    }

    private void detectFeeOutliers(TransactionData transaction, AnalysisResult result) {
        double feePerByte = transaction.getFeePerByte();

        // Z-score based outlier detection
        double zScore = Math.abs((feePerByte - currentStats.mean) / currentStats.stdDev);
        if (zScore > OUTLIER_THRESHOLD) {
            String severity = zScore > 4.0 ? "EXTREME" : "HIGH";
            result.addInsight(
                    String.format("Fee outlier detected: %.2f sat/byte (z-score: %.2f, %s)",
                            feePerByte, zScore, severity),
                    Insight.InsightType.OUTLIER
            );
        }

        // IQR based outlier detection
        if (feePerByte > currentStats.q3 + 1.5 * currentStats.iqr) {
            result.addInsight(
                    String.format("High totalFee outlier: %.2f sat/byte (>Q3+1.5*IQR: %.2f)",
                            feePerByte, currentStats.q3 + 1.5 * currentStats.iqr),
                    Insight.InsightType.OUTLIER
            );
        }
    }

    private void detectSpamTransactions(TransactionData transaction, AnalysisResult result) {
        double feePerByte = transaction.getFeePerByte();

        // Very low totalFee compared to median
        if (feePerByte < currentStats.median * 0.1 && feePerByte < SPAM_FEE_THRESHOLD) {
            result.addInsight(
                    String.format("Potential spam TX: Very low totalFee %.4f sat/byte (median: %.2f)",
                            feePerByte, currentStats.median),
                    Insight.InsightType.SPAM
            );
        }

        // Large transaction with low totalFee (potential spam pattern)
        if (transaction.size() > 1000 && feePerByte < currentStats.percentile10) {
            result.addInsight(
                    String.format("Large low-totalFee TX: %d bytes, %.4f sat/byte",
                            transaction.size(), feePerByte),
                    Insight.InsightType.SPAM
            );
        }
    }

    private void detectFeeSurges(TransactionData transaction, AnalysisResult result) {
        // Get recent transactions (last 100)
        List<TransactionData> recentTxs = transactionWindow.stream()
                .skip(Math.max(0, transactionWindow.size() - 100))
                .collect(Collectors.toList());

        if (recentTxs.size() < 20) return;

        // Calculate moving averages
        double recentAverage = recentTxs.stream()
                .mapToDouble(TransactionData::getFeePerByte)
                .average().orElse(0.0);

        double olderAverage = transactionWindow.stream()
                .limit(transactionWindow.size() - 100)
                .mapToDouble(TransactionData::getFeePerByte)
                .average().orElse(recentAverage);

        // Detect surge
        if (recentAverage > olderAverage * 2.0) {
            result.addInsight(
                    String.format("Fee surge detected: Recent avg %.2f vs older avg %.2f sat/byte",
                            recentAverage, olderAverage),
                    Insight.InsightType.SURGE
            );
        }
    }

//    private void analyzeBlockPatterns() {
//        if (blockWindow.size() < 3) return;
//
//        List<BlockData> blocks = new ArrayList<>(blockWindow);
//        BlockData latestBlock = blocks.get(blocks.size() - 1);
//
//        // Detect totalFee wars (consecutive blocks with increasing median fees)
//        detectFeeWars(blocks);
//
//        // Detect full blocks with low fees (potential spam or unusual patterns)
//        detectFullBlocksLowFees(latestBlock);
//
//        // Detect mempool pressure patterns
//        detectMempoolPressurePatterns(blocks);
//    }

    private void detectFeeWars(List<BlockData> blocks) {
        if (blocks.size() < 3) return;

        int consecutiveIncreases = 0;
        for (int i = blocks.size() - 2; i >= 1; i--) {
            if (blocks.get(i).medianFeePerByte() > blocks.get(i - 1).medianFeePerByte() * 1.2) {
                consecutiveIncreases++;
            } else {
                break;
            }
        }

        if (consecutiveIncreases >= 2) {
            System.out.println("🔥 FEE WAR DETECTED: " + (consecutiveIncreases + 1) +
                    " consecutive blocks with increasing fees");
        }
    }

//    private void detectFullBlocksLowFees(BlockData block) {
//        // Block is considered full if > 90% of max size
//        boolean isFullBlock = block.size() > block.maxSize() * 0.9;
//        boolean hasLowFees = block.medianFeePerByte() < currentStats.percentile25;
//
//        if (isFullBlock && hasLowFees) {
//            System.out.println("⚠️ ANOMALY: Full block with low fees - Block: " +
//                    block.height() + ", Size: " + block.size() +
//                    ", Median totalFee: " + block.medianFeePerByte() + " sat/byte");
//        }
//    }

    private void detectMempoolPressurePatterns(List<BlockData> blocks) {
        if (blocks.size() < 5) return;

        // Check if recent blocks are consistently full with high fees
        long fullBlocks = blocks.stream()
                .skip(blocks.size() - 5)
                .mapToLong(block -> block.size() > block.maxSize() * 0.95 ? 1 : 0)
                .sum();

        double avgRecentFees = blocks.stream()
                .skip(blocks.size() - 5)
                .mapToDouble(BlockData::medianFeePerByte)
                .average().orElse(0.0);

        if (fullBlocks >= 4 && avgRecentFees > currentStats.percentile75) {
            System.out.println("🚨 HIGH MEMPOOL PRESSURE: 4+ consecutive full blocks with high fees");
        }
    }

    private void updateStatistics() {
        if (transactionWindow.size() < MIN_TRANSACTIONS_FOR_ANALYSIS) return;

        List<Double> fees = transactionWindow.stream()
                .mapToDouble(TransactionData::getFeePerByte)
                .sorted()
                .boxed()
                .collect(Collectors.toList());

        currentStats = calculateStatistics(fees);
        lastAnalysisTime = System.currentTimeMillis();
    }

    private FeeStatistics calculateStatistics(List<Double> sortedFees) {
        FeeStatistics stats = new FeeStatistics();

        // Basic statistics
        stats.mean = sortedFees.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        stats.median = calculatePercentile(sortedFees, 50);
        stats.min = sortedFees.get(0);
        stats.max = sortedFees.get(sortedFees.size() - 1);

        // Quartiles and IQR
        stats.q1 = calculatePercentile(sortedFees, 25);
        stats.q3 = calculatePercentile(sortedFees, 75);
        stats.iqr = stats.q3 - stats.q1;

        // Percentiles
        stats.percentile10 = calculatePercentile(sortedFees, 10);
        stats.percentile25 = stats.q1;
        stats.percentile75 = stats.q3;
        stats.percentile90 = calculatePercentile(sortedFees, 90);

        // Standard deviation
        double variance = sortedFees.stream()
                .mapToDouble(totalFee -> Math.pow(totalFee - stats.mean, 2))
                .average().orElse(0.0);
        stats.stdDev = Math.sqrt(variance);

        return stats;
    }

    private double calculatePercentile(List<Double> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) return 0.0;

        double index = (percentile / 100.0) * (sortedValues.size() - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);

        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        }

        double weight = index - lowerIndex;
        return sortedValues.get(lowerIndex) + weight * (sortedValues.get(upperIndex) - sortedValues.get(lowerIndex));
    }

    public String getStatsSummary() {
        if (transactionWindow.size() < MIN_TRANSACTIONS_FOR_ANALYSIS) {
            return "Insufficient data for statistics";
        }

        return String.format(
                "Fee Statistics (last %d txs):\n" +
                        "  Mean: %.2f sat/byte\n" +
                        "  Median: %.2f sat/byte\n" +
                        "  P10/P90: %.2f/%.2f sat/byte\n" +
                        "  Q1/Q3: %.2f/%.2f sat/byte\n" +
                        "  Min/Max: %.4f/%.2f sat/byte\n" +
                        "  Std Dev: %.2f\n" +
                        "  Last updated: %s",
                transactionWindow.size(),
                currentStats.mean, currentStats.median,
                currentStats.percentile10, currentStats.percentile90,
                currentStats.q1, currentStats.q3,
                currentStats.min, currentStats.max,
                currentStats.stdDev,
                Instant.ofEpochMilli(lastAnalysisTime)
        );
    }


    private static class FeeStatistics {
        double mean, median, min, max;
        double q1, q3, iqr;
        double percentile10, percentile25, percentile75, percentile90;
        double stdDev;
    }
    **/
}
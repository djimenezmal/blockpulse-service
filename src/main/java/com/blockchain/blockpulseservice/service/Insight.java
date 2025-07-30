package com.blockchain.blockpulseservice.service;

public record Insight(String message, InsightType type,
                      long timestamp) {

    @Override
    public String toString() {
        return String.format("[%s] %s", type, message);
    }

    public enum InsightType {
        OUTLIER, SPAM, SURGE, PATTERN, INFO
    }
}

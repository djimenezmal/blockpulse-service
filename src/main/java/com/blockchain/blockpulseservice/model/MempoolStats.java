package com.blockchain.blockpulseservice.model;

public record MempoolStats(double fastFee, double mediumFee, double slowFee, int mempoolSize) {}
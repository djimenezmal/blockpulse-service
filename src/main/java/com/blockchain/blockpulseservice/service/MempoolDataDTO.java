package com.blockchain.blockpulseservice.service;

public record MempoolDataDTO(double fastFee, double mediumFee, double slowFee, int mempoolSize) {}

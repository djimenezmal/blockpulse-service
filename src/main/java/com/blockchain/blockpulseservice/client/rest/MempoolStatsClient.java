package com.blockchain.blockpulseservice.client.rest;

import com.blockchain.blockpulseservice.dto.RecommendedTransactionFeeDTO;
import com.blockchain.blockpulseservice.dto.MempoolInfoDTO;
import com.blockchain.blockpulseservice.model.MempoolStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class MempoolStatsClient {
    private final String feeApiUrl;
    private final String mempoolInfoUrl;
    private final RestTemplate restTemplate;
    private volatile MempoolStats mempoolStats;

    public MempoolStatsClient(@Value("${app.mempool.space.rest.fee-api-url}") String feeApiUrl,
                              @Value("${app.mempool.space.rest.mempool-info-api-url}") String mempoolInfoUrl,
                              RestTemplate restTemplate) {
        this.feeApiUrl = feeApiUrl;
        this.mempoolInfoUrl = mempoolInfoUrl;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void updateMempoolStats() {
        log.info("Updating mempool data...");
        try {
            var feeDto = restTemplate.getForObject(feeApiUrl, RecommendedTransactionFeeDTO.class);
            var mempoolInfoDTO = restTemplate.getForObject(mempoolInfoUrl, MempoolInfoDTO.class);
            var mempoolStats = new MempoolStats(feeDto.fastestFee(), feeDto.halfHourFee(), feeDto.hourFee(), mempoolInfoDTO.memPoolSize());
            this.mempoolStats = mempoolStats;
            log.info("Updated mempool data: {}", mempoolStats);
        } catch (Exception e) {
            log.error("Failed to fetch mempool data: {}", e.getMessage());
        }
    }

    public MempoolStats getMempoolStats() {
        return mempoolStats == null ? new MempoolStats(0, 0, 0, 0) : mempoolStats;
    }
}
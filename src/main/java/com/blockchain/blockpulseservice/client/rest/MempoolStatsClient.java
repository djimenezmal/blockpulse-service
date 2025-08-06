package com.blockchain.blockpulseservice.client.rest;

import com.blockchain.blockpulseservice.dto.RecommendedTransactionFeeDTO;
import com.blockchain.blockpulseservice.dto.MempoolInfoDTO;
import com.blockchain.blockpulseservice.model.MempoolStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.*;

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
        try (var executorService = Executors.newSingleThreadExecutor()) {

            CompletableFuture<RecommendedTransactionFeeDTO> feeFuture = CompletableFuture
                    .supplyAsync(() -> restTemplate.getForObject(feeApiUrl, RecommendedTransactionFeeDTO.class), executorService)
                    .orTimeout(2, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        log.error("Failed to fetch fee data: {}", ex.getMessage());
                        return null;
                    });

            CompletableFuture<MempoolInfoDTO> mempoolFuture = CompletableFuture
                    .supplyAsync(() -> restTemplate.getForObject(mempoolInfoUrl, MempoolInfoDTO.class), executorService)
                    .orTimeout(2, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        log.error("Failed to fetch mempool info: {}", ex.getMessage());
                        return null;
                    });

            CompletableFuture<Void> all = CompletableFuture.allOf(feeFuture, mempoolFuture);

            all.join(); // Wait for both tasks to finish

            var feeDto = feeFuture.getNow(null);
            var mempoolInfoDTO = mempoolFuture.getNow(null);

            if (feeDto != null && mempoolInfoDTO != null) {
                var mempoolStats = mapToMempoolInfo(feeDto, mempoolInfoDTO);
                this.mempoolStats = mempoolStats;
                log.info("Updated mempool data: {}", mempoolStats);
            } else {
                log.warn("One or both API calls returned null. Skipping mempool stats update.");
            }

        } catch (Exception e) {
            log.error("Unexpected error while fetching mempool data", e);
        }

    }

    private MempoolStats mapToMempoolInfo() {
        return new MempoolStats(feeDto.fastestFee(), feeDto.halfHourFee(), feeDto.hourFee(), mempoolInfoDTO.memPoolSize());
    }

    public MempoolStats getMempoolStats() {
        return mempoolStats == null ? new MempoolStats(0, 0, 0, 0) : mempoolStats;
    }
}
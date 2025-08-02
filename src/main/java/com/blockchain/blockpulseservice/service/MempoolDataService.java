package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.config.rest.FeeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class MempoolDataService {

    private final String feeApiUrl;
    private final String mempoolInfoUrl;
    private final RestTemplate restTemplate;
    private volatile MempoolDataDTO currentMempoolDataDTO;

    public MempoolDataService(@Value("${app.rest.mempool.space.fee-api-url}") String feeApiUrl,
                              @Value("${app.rest.mempool.space.mempool-info-api-url}") String mempoolInfoUrl,
                              RestTemplate restTemplate) {
        this.feeApiUrl = feeApiUrl;
        this.mempoolInfoUrl = mempoolInfoUrl;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void updateMempoolData() {
        try {
            var feeDto = restTemplate.getForObject(feeApiUrl, FeeDTO.class);
            var mempoolInfoDTO = restTemplate.getForObject(mempoolInfoUrl, MempoolInfoDTO.class);
            currentMempoolDataDTO = new MempoolDataDTO(feeDto.fastFee(), feeDto.mediumFee(), feeDto.slowFee(), mempoolInfoDTO.memPoolSize());

            log.info("Updated mempool data: {}", currentMempoolDataDTO);
        } catch (Exception e) {
            log.error("Failed to fetch mempool data: {}", e.getMessage());
        }
    }

    public MempoolDataDTO getCurrentMempoolData() {
        return currentMempoolDataDTO;
    }
}

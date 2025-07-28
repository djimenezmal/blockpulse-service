package com.blockchain.blockpulseservice;

import com.crypto.feemarketcomparator.dto.FeeAnalysisDTO;
import com.crypto.feemarketcomparator.model.Transaction;
import com.crypto.feemarketcomparator.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    
    private final TransactionService transactionService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/subscribe/btc")
    @SendTo("/topic/analysis/btc")
    public FeeAnalysisDTO subscribeToBtcAnalysis() {
        log.info("Client subscribed to BTC analysis");
        return transactionService.getCurrentFeeAnalysis(Transaction.CryptoCurrency.BTC);
    }
    
    @MessageMapping("/subscribe/kaspa")
    @SendTo("/topic/analysis/kaspa")
    public FeeAnalysisDTO subscribeToKaspaAnalysis() {
        log.info("Client subscribed to Kaspa analysis");
        return transactionService.getCurrentFeeAnalysis(Transaction.CryptoCurrency.KASPA);
    }
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void sendPeriodicBtcAnalysis() {
        try {
            FeeAnalysisDTO analysis = transactionService.getCurrentFeeAnalysis(Transaction.CryptoCurrency.BTC);
            messagingTemplate.convertAndSend("/topic/analysis/btc", analysis);
        } catch (Exception e) {
            log.error("Error sending periodic BTC analysis", e);
        }
    }
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void sendPeriodicKaspaAnalysis() {
        try {
            FeeAnalysisDTO analysis = transactionService.getCurrentFeeAnalysis(Transaction.CryptoCurrency.KASPA);
            messagingTemplate.convertAndSend("/topic/analysis/kaspa", analysis);
        } catch (Exception e) {
            log.error("Error sending periodic Kaspa analysis", e);
        }
    }
}
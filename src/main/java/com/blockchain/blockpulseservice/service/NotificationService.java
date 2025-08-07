package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.AnalyzedTransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendAnalysis(AnalyzedTransactionDTO analyzedTransactionDTO) {
        messagingTemplate.convertAndSend("/topic/transactions", analyzedTransactionDTO);
        log.info("Sent transaction analysis message: {}", analyzedTransactionDTO);
    }
}

package com.blockchain.blockpulseservice;

import com.crypto.feemarketcomparator.model.Transaction;
import com.crypto.feemarketcomparator.service.TransactionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;

@Slf4j
@Component
public class KaspaWebSocketClient extends BaseWebSocketClient {
    
    private final TransactionService transactionService;
    
    @Autowired
    public KaspaWebSocketClient(ObjectMapper objectMapper, TransactionService transactionService) {
        // Using kaspa-live API as an example - adjust based on actual Kaspa API
        super(URI.create("wss://api.kaspa.org/ws"), objectMapper);
        this.transactionService = transactionService;
    }
    
    @Override
    protected void onConnected() {
        // Subscribe to Kaspa transactions and blocks
        send("{\"type\":\"subscribe\",\"data\":{\"transactions\":true,\"blocks\":true}}");
    }
    
    @Override
    protected void processMessage(String message) throws Exception {
        JsonNode node = objectMapper.readTree(message);
        String type = node.get("type").asText();
        
        switch (type) {
            case "transaction" -> processNewTransaction(node.get("data"));
            case "block" -> processNewBlock(node.get("data"));
            default -> log.debug("Unknown Kaspa message type: {}", type);
        }
    }
    
    private void processNewTransaction(JsonNode txNode) {
        try {
            String hash = txNode.get("hash").asText();
            long size = txNode.get("size").asLong();
            long fee = txNode.get("fee").asLong();
            
            BigDecimal feePerByte = BigDecimal.valueOf(fee).divide(BigDecimal.valueOf(size), 8, BigDecimal.ROUND_HALF_UP);
            
            Transaction transaction = Transaction.builder()
                .txHash(hash)
                .currency(Transaction.CryptoCurrency.KASPA)
                .transactionSize((int) size)
                .totalFee(BigDecimal.valueOf(fee).divide(BigDecimal.valueOf(100_000_000), 8, BigDecimal.ROUND_HALF_UP))
                .feePerByte(feePerByte)
                .timestamp(LocalDateTime.now())
                .build();
            
            transactionService.processNewTransaction(transaction);
            
        } catch (Exception e) {
            log.error("Error processing Kaspa transaction", e);
        }
    }
    
    private void processNewBlock(JsonNode blockNode) {
        try {
            long height = blockNode.get("height").asLong();
            
            transactionService.updateConfirmedTransactions(Transaction.CryptoCurrency.KASPA, height, LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error processing Kaspa block", e);
        }
    }
}
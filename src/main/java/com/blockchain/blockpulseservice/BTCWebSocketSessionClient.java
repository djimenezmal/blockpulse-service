package com.blockchain.blockpulseservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Component
public class BTCWebSocketSessionClient extends BaseWebSocketSessionClient {

    private final TransactionService transactionService;
    private volatile boolean subscribed = false;

    @Autowired
    public BTCWebSocketSessionClient(WebSocketClient webSocketClient,
                                     ObjectMapper objectMapper,
                                     ScheduledExecutorService scheduler,
                                     TransactionService transactionService) {
        super(webSocketClient, objectMapper, scheduler, URI.create("wss://ws.blockchain.info/inv"));
        this.transactionService = transactionService;
    }

    @Override
    protected void onConnectionEstablished(WebSocketSession session) {
        log.info("BTC WebSocket connection established. Session ID: {}", session.getId());

        try {
            // Subscribe to unconfirmed transactions
            sendMessage("{\"op\":\"unconfirmed_sub\"}");

            // Subscribe to new blocks
            sendMessage("{\"op\":\"blocks_sub\"}");

            // Subscribe to address transactions (optional - for specific addresses)
            // sendMessage("{\"op\":\"addr_sub\", \"addr\":\"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa\"}");

            subscribed = true;
            log.info("Successfully subscribed to BTC WebSocket events");

        } catch (Exception e) {
            log.error("Failed to subscribe to BTC WebSocket events", e);
        }
    }

    @Override
    protected void processMessage(String message) throws Exception {
        JsonNode node = objectMapper.readTree(message);

        if (!node.has("op")) {
            log.debug("Received message without 'op' field: {}", message.substring(0, Math.min(100, message.length())));
            return;
        }

        String operation = node.get("op").asText();

        switch (operation) {
            case "utx" -> {
                if (node.has("x")) {
                    processUnconfirmedTransaction(node.get("x"));
                }
            }
            case "block" -> {
                if (node.has("x")) {
                    processNewBlock(node.get("x"));
                }
            }
            case "status" -> {
                processStatusMessage(node);
            }
            default -> {
                log.debug("Unknown BTC operation: {}", operation);
            }
        }
    }

    private void processUnconfirmedTransaction(JsonNode txNode) {
        try {
            if (!txNode.has("hash") || !txNode.has("size") || !txNode.has("fee")) {
                log.debug("Transaction missing required fields");
                return;
            }

            String hash = txNode.get("hash").asText();
            long size = txNode.get("size").asLong();
            long feeValue = txNode.get("fee").asLong(); // Fee in satoshis

            // Get timestamp
            long timestamp = txNode.has("time") ?
                    txNode.get("time").asLong() :
                    Instant.now().getEpochSecond();

            // Validate data
            if (size <= 0 || feeValue < 0) {
                log.debug("Invalid transaction data - size: {}, fee: {}", size, feeValue);
                return;
            }

            // Calculate fee per byte
            BigDecimal feePerByte = BigDecimal.valueOf(feeValue)
                    .divide(BigDecimal.valueOf(size), 8, RoundingMode.HALF_UP);

            // Convert fee to BTC (from satoshis)
            BigDecimal totalFeeBTC = BigDecimal.valueOf(feeValue)
                    .divide(BigDecimal.valueOf(100_000_000), 8, RoundingMode.HALF_UP);

            // Create transaction object
            Transaction transaction = Transaction.builder()
                    .txHash(hash)
                    .currency(Transaction.CryptoCurrency.BTC)
                    .blockHeight(0L) // Unconfirmed
                    .transactionSize((int) size)
                    .totalFee(totalFeeBTC)
                    .feePerByte(feePerByte)
                    .timestamp(LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC))
                    .build();

            // Process transaction
            transactionService.processNewTransaction(transaction);

            log.debug("Processed BTC unconfirmed transaction: {} - Size: {} bytes, Fee: {} sat/byte",
                    hash.substring(0, 8), size, feePerByte);

        } catch (Exception e) {
            log.error("Error processing BTC unconfirmed transaction", e);
        }
    }

    private void processNewBlock(JsonNode blockNode) {
        try {
            if (!blockNode.has("height")) {
                log.debug("Block missing height field");
                return;
            }

            long height = blockNode.get("height").asLong();

            // Get block timestamp
            long timestamp = blockNode.has("time") ?
                    blockNode.get("time").asLong() :
                    Instant.now().getEpochSecond();

            LocalDateTime confirmedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);

            // Update confirmed transactions
            transactionService.updateConfirmedTransactions(
                    Transaction.CryptoCurrency.BTC, height, confirmedAt);

            log.info("Processed BTC block {} at height {}",
                    blockNode.has("hash") ? blockNode.get("hash").asText().substring(0, 8) : "unknown",
                    height);

        } catch (Exception e) {
            log.error("Error processing BTC block", e);
        }
    }

    private void processStatusMessage(JsonNode statusNode) {
        if (statusNode.has("status")) {
            String status = statusNode.get("status").asText();
            log.info("BTC WebSocket status: {}", status);

            if ("subscribed".equals(status)) {
                subscribed = true;
            }
        }
    }

    @Override
    protected void onConnectionLost() {
        subscribed = false;
        log.warn("BTC WebSocket connection lost. Will attempt to reconnect...");
    }

    public boolean isSubscribed() {
        return subscribed && isConnected();
    }

    // Method to subscribe to specific address transactions
    public void subscribeToAddress(String address) {
        if (!isConnected()) {
            log.warn("Cannot subscribe to address - not connected");
            return;
        }

        try {
            String subscriptionMessage = String.format("{\"op\":\"addr_sub\", \"addr\":\"%s\"}", address);
            sendMessage(subscriptionMessage);
            log.info("Subscribed to BTC address: {}", address);
        } catch (Exception e) {
            log.error("Failed to subscribe to BTC address: {}", address, e);
        }
    }

    // Method to unsubscribe from address transactions
    public void unsubscribeFromAddress(String address) {
        if (!isConnected()) {
            log.warn("Cannot unsubscribe from address - not connected");
            return;
        }

        try {
            String unsubscriptionMessage = String.format("{\"op\":\"addr_unsub\", \"addr\":\"%s\"}", address);
            sendMessage(unsubscriptionMessage);
            log.info("Unsubscribed from BTC address: {}", address);
        } catch (Exception e) {
            log.error("Failed to unsubscribe from BTC address: {}", address, e);
        }
    }

    // Method to get connection statistics
    public String getConnectionStatus() {
        return String.format("BTC WebSocket - Connected: %s, Subscribed: %s, Session: %s",
                isConnected(), subscribed,
                getSession() != null ? getSession().getId() : "null");
    }
}
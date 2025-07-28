package com.blockchain.blockpulseservice;

import com.blockchain.blockpulseservice.wsclient.BaseWebSocketSessionClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class BlockchainInfoWebSocketClient extends BaseWebSocketSessionClient {
    
    private static final String BLOCKCHAIN_INFO_WS_URL = "wss://ws.blockchain.info/inv";
    
    private final TransactionMapper transactionMapper;

    public BlockchainInfoWebSocketClient(WebSocketClient webSocketClient,
                                         ObjectMapper objectMapper,
                                         ScheduledExecutorService scheduler,
                                         TransactionMapper transactionMapper) {
        super(webSocketClient, objectMapper, scheduler, URI.create(BLOCKCHAIN_INFO_WS_URL));
        this.transactionMapper = transactionMapper;
    }

    @Override
    protected void onConnectionEstablished(WebSocketSession session) {
        log.info("Connected to Blockchain.info WebSocket");
        
        // Subscribe to unconfirmed transactions
        subscribeToUnconfirmedTransactions();
        
        // Subscribe to new blocks (which contain confirmed transactions)
        subscribeToNewBlocks();
    }
    
    @Override
    protected void processMessage(String message) throws Exception {
        log.debug("Processing message: {}", message.substring(0, Math.min(200, message.length())));
        
        try {
            BlockchainInfoWebSocketMessage wsMessage = objectMapper.readValue(message, BlockchainInfoWebSocketMessage.class);
            
            if (wsMessage.getOperation() != null && wsMessage.getTransaction() != null) {
                Transaction transaction = transactionMapper.mapToTransaction(wsMessage.getTransaction());
                
                switch (wsMessage.getOperation()) {
                    case "utx":
                        // Unconfirmed transaction
                        break;
                    case "block":
                        // Transaction in a new block (confirmed)
                        break;
                    default:
                        log.debug("Unknown operation type: {}", wsMessage.getOperation());
                }
            }
        } catch (Exception e) {
            log.error("Error processing blockchain.info message: {}", message, e);
        }
    }
    
    @Override
    protected void onConnectionLost() {
        log.warn("Lost connection to Blockchain.info WebSocket");
    }
    
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        log.debug("Received binary message from Blockchain.info ({} bytes)", message.getPayloadLength());
        // Blockchain.info typically uses text messages, but we can handle binary if needed
    }
    
    private void subscribeToUnconfirmedTransactions() {
        String subscribeMessage = "{\"op\":\"unconfirmed_sub\"}";
        sendMessage(subscribeMessage);
        log.info("Subscribed to unconfirmed transactions");
    }
    
    private void subscribeToNewBlocks() {
        String subscribeMessage = "{\"op\":\"blocks_sub\"}";
        sendMessage(subscribeMessage);
        log.info("Subscribed to new blocks");
    }
    
    public void subscribeToAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            log.warn("Cannot subscribe to empty address");
            return;
        }
        
        String subscribeMessage = String.format("{\"op\":\"addr_sub\", \"addr\":\"%s\"}", address);
        sendMessage(subscribeMessage);
        log.info("Subscribed to address: {}", address);
    }
    
    public void unsubscribeFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            log.warn("Cannot unsubscribe from empty address");
            return;
        }
        
        String unsubscribeMessage = String.format("{\"op\":\"addr_unsub\", \"addr\":\"%s\"}", address);
        sendMessage(unsubscribeMessage);
        log.info("Unsubscribed from address: {}", address);
    }
}
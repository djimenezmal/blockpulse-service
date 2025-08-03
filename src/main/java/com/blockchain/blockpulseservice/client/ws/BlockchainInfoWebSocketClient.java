package com.blockchain.blockpulseservice.client.ws;

import com.blockchain.blockpulseservice.service.FeeSurgeService;
import com.blockchain.blockpulseservice.mapper.TransactionMapper;
import com.blockchain.blockpulseservice.dto.TransactionDTOWrapper;
import com.blockchain.blockpulseservice.client.ws.manager.ConnectionStateManager;
import com.blockchain.blockpulseservice.client.ws.manager.ReconnectionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;


@Slf4j
@Component
public class BlockchainInfoWebSocketClient extends BaseWebSocketSessionClient {
    private final TransactionMapper transactionMapper;
    private final ObjectMapper objectMapper;
    private final FeeSurgeService feeSurgeService;

    public BlockchainInfoWebSocketClient(TransactionMapper transactionMapper,
                                         ObjectMapper objectMapper,
                                         FeeSurgeService feeSurgeService,
                                         WebSocketClient webSocketClient,
                                         ConnectionStateManager connectionState,
                                         ReconnectionManager reconnectionManager,
                                         WebSocketMessageHandler messageHandler,
                                         WebSocketMessageSender messageSender,
                                         @Value("${app.websocket.blockchain.info.ws.uri}") String serverUri,
                                         @Value("${app.websocket.message-size-limit}") int messageSizeLimit) {
        super(URI.create(serverUri),
                messageSizeLimit,
                webSocketClient,
                connectionState,
                reconnectionManager,
                messageHandler,
                messageSender);
        this.transactionMapper = transactionMapper;
        this.objectMapper = objectMapper;
        this.feeSurgeService = feeSurgeService;
    }

    @Override
    protected void onConnectionEstablished(WebSocketSession session) {
        log.info("Connected to Blockchain.info WebSocket");
        subscribeToUnconfirmedTransactions();
        //subscribeToNewBlocks();
    }

    @Override
    protected void processMessage(String message) {
        log.debug("Processing message: {}", message.substring(0, Math.min(200, message.length())));

        try {
            var txWrapper = objectMapper.readValue(message, TransactionDTOWrapper.class);
            var tx = transactionMapper.mapToTransaction(txWrapper.transactionDTO());
            log.info("Mapped transaction: {}", tx.toString());
            feeSurgeService.processTransaction(tx);
        } catch (Exception e) {
            log.error("Error processing blockchain.info message: {}", message, e);
        }
    }

    private void subscribeToUnconfirmedTransactions() {
        var subscribeMessage = "{\"op\":\"unconfirmed_sub\"}";
        sendMessage(subscribeMessage);
        log.info("Subscribed to unconfirmed transactions");
    }

//    private void subscribeToNewBlocks() {
//        String subscribeMessage = "{\"op\":\"blocks_sub\"}";
//        sendMessage(subscribeMessage);
//        log.info("Subscribed to new blocks");
//    }
//
//    public void subscribeToAddress(String address) {
//        if (address == null || address.trim().isEmpty()) {
//            log.warn("Cannot subscribe to empty address");
//            return;
//        }
//
//        String subscribeMessage = String.format("{\"op\":\"addr_sub\", \"addr\":\"%s\"}", address);
//        sendMessage(subscribeMessage);
//        log.info("Subscribed to address: {}", address);
//    }
//
//    public void unsubscribeFromAddress(String address) {
//        if (address == null || address.trim().isEmpty()) {
//            log.warn("Cannot unsubscribe from empty address");
//            return;
//        }
//
//        String unsubscribeMessage = String.format("{\"op\":\"addr_unsub\", \"addr\":\"%s\"}", address);
//        sendMessage(unsubscribeMessage);
//        log.info("Unsubscribed from address: {}", address);
//    }
}
package com.blockchain.blockpulseservice.client.ws;

import com.blockchain.blockpulseservice.client.ws.manager.ConnectionStateManager;
import com.blockchain.blockpulseservice.client.ws.manager.ReconnectionManager;
import com.blockchain.blockpulseservice.dto.MempoolTransactionsDTOWrapper;
import com.blockchain.blockpulseservice.mapper.TransactionMapper;
import com.blockchain.blockpulseservice.service.TransactionAnalyzerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import java.net.URI;


@Slf4j
@Component
public class MempoolSpaceWebSocketClient extends BaseWebSocketSessionClient {
    private final TransactionMapper transactionMapper;
    private final ObjectMapper objectMapper;
    private final TransactionAnalyzerService transactionAnalyzerService;

    public MempoolSpaceWebSocketClient(TransactionMapper transactionMapper,
                                       ObjectMapper objectMapper,
                                       TransactionAnalyzerService transactionAnalyzerService,
                                       WebSocketClient webSocketClient,
                                       ConnectionStateManager connectionState,
                                       ReconnectionManager reconnectionManager,
                                       WebSocketMessageHandler messageHandler,
                                       WebSocketMessageSender messageSender,
                                       @Value("${app.mempool.space.websocket.track-mempool-api-url}") String serverUri,
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
        this.transactionAnalyzerService = transactionAnalyzerService;
    }

    @Override
    protected void onConnectionEstablished(WebSocketSession session) {
        log.info("Connected to {}", serverUri);
        subscribeToTrackMempoolTransactions();
    }

    @Override
    protected void processMessage(String message) {
        log.debug("Processing message: {}", message.substring(0, Math.min(200, message.length())));

        try {
            var txWrapper = objectMapper.readValue(message, MempoolTransactionsDTOWrapper.class);
            var txs = transactionMapper.mapToTransaction(txWrapper.mempoolTransactions().added());
            log.info("Mapped {} transactions", txs.size());
            transactionAnalyzerService.processTransaction(txs);
            //log.info("Mapped transaction: {}", txs.toString());
        } catch (Exception e) {
            log.error("Error processing blockchain.info message: {}", message, e);
        }
    }

    private void subscribeToTrackMempoolTransactions() {
        var subscribeMessage = "{ \"track-mempool\": true }";
        sendMessage(subscribeMessage);
        log.info("Subscribed to track mempool transactions");
    }
}
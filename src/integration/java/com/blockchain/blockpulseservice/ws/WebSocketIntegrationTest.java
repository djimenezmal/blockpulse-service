package com.blockchain.blockpulseservice.ws;

import com.blockchain.blockpulseservice.model.AnalyzedTransactionDTO;
import com.blockchain.blockpulseservice.model.FeeClassification;
import com.blockchain.blockpulseservice.model.PatternType;
import com.blockchain.blockpulseservice.model.TransactionWindowSnapshotDTO;
import com.blockchain.blockpulseservice.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private NotificationService notificationService;

    private WebSocketStompClient stompClient;
    private StompSession session;

    @BeforeEach
    void setup() throws Exception {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        session = stompClient.connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {}).get(3, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }

    @Test
    void clientReceivesTransactionsMessage() throws Exception {
        CompletableFuture<AnalyzedTransactionDTO> future = new CompletableFuture<>();
        session.subscribe("/topic/transactions", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return AnalyzedTransactionDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                future.complete((AnalyzedTransactionDTO) payload);
            }
        });

        var dto = AnalyzedTransactionDTO.builder()
                .id("tx1")
                .feePerVByte(1.0)
                .totalFee(1.0)
                .size(1)
                .timestamp(1L)
                .patterns(Set.of(PatternType.SURGE))
                .feeClassification(FeeClassification.CHEAP)
                .isOutlier(false)
                .windowSnapshotDTO(new TransactionWindowSnapshotDTO(1, 1.0, 1.0, 0))
                .build();

        notificationService.sendAnalysis(dto);

        AnalyzedTransactionDTO received = future.get(5, TimeUnit.SECONDS);
        assertThat(received).isEqualTo(dto);
    }
}
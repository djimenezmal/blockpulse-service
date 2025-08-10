package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.AnalyzedTransactionDTO;
import com.blockchain.blockpulseservice.model.FeeClassification;
import com.blockchain.blockpulseservice.model.PatternType;
import com.blockchain.blockpulseservice.model.TransactionWindowSnapshotDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    void subscriberReceivesTransactionsMessage() throws Exception {
        BlockingQueue<AnalyzedTransactionDTO> queue = new ArrayBlockingQueue<>(1);

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE) {
            @Override
            protected void sendInternal(Object object, MediaType mediaType) throws IOException {
                queue.offer((AnalyzedTransactionDTO) object);
            }
        };

        notificationService.subscribe(emitter);

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

        AnalyzedTransactionDTO received = queue.poll(3, TimeUnit.SECONDS);
        assertNotNull(received);
        assertEquals(dto, received);
    }
}

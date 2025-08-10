package com.blockchain.blockpulseservice.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.blockchain.blockpulseservice.model.AnalyzedTransactionDTO;
import com.blockchain.blockpulseservice.model.FeeClassification;
import com.blockchain.blockpulseservice.model.PatternType;
import com.blockchain.blockpulseservice.model.TransactionWindowSnapshotDTO;
import com.blockchain.blockpulseservice.service.AnalyzedTransactionEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
class TransactionStreamControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CopyOnWriteArrayList<SseEmitter> emitters;

    @Autowired
    private AnalyzedTransactionEmitter analyzedTransactionEmitter;

    @AfterEach
    void tearDown() {
        emitters.clear();
    }

    @Test
    void streamEndpointRegistersEmitterAndDeliversMessage() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/stream"))
                .andExpect(request().asyncStarted())
                .andReturn();

        assertEquals(1, emitters.size());

        var queue = new ArrayBlockingQueue<AnalyzedTransactionDTO>(1);

        var capturingEmitter = new SseEmitter(Long.MAX_VALUE) {
            @Override
            protected void sendInternal(Object object, MediaType mediaType) throws IOException {
                queue.offer((AnalyzedTransactionDTO) object);
            }
        };

        emitters.set(0, capturingEmitter);

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

        analyzedTransactionEmitter.sendAnalysis(dto);

        var received = queue.poll(3, TimeUnit.SECONDS);
        assertNotNull(received);
        assertEquals(dto, received);
    }
}

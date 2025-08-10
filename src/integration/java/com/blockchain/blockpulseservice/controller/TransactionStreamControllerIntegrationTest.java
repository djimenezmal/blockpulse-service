package com.blockchain.blockpulseservice.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
class TransactionStreamControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CopyOnWriteArrayList<SseEmitter> emitters;

    @AfterEach
    void tearDown() {
        emitters.clear();
    }

    @Test
    void callingStreamEndpointRegistersEmitter() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/stream"))
                .andExpect(request().asyncStarted());

        assertEquals(1, emitters.size());
    }
}

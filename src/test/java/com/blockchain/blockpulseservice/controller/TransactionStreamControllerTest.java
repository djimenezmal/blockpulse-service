package com.blockchain.blockpulseservice.controller;

import com.blockchain.blockpulseservice.service.AnalyzedTransactionEmitter;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionStreamControllerTest {

    @Test
    void streamTransactionsDelegatesToEmitter() {
        var emitterService = mock(AnalyzedTransactionEmitter.class);
        var emitter = new SseEmitter();
        when(emitterService.subscribe()).thenReturn(emitter);

        var controller = new TransactionStreamController(emitterService);
        var result = controller.streamTransactions();

        assertSame(emitter, result);
        verify(emitterService).subscribe();
    }
}

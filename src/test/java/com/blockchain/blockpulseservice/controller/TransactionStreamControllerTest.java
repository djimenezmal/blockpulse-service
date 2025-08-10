package com.blockchain.blockpulseservice.controller;

import com.blockchain.blockpulseservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionStreamControllerTest {

    @Test
    void streamTransactionsDelegatesToNotificationService() {
        NotificationService service = mock(NotificationService.class);
        SseEmitter emitter = new SseEmitter();
        when(service.subscribe()).thenReturn(emitter);

        TransactionStreamController controller = new TransactionStreamController(service);
        SseEmitter result = controller.streamTransactions();

        assertSame(emitter, result);
        verify(service).subscribe();
    }
}

package com.blockchain.blockpulseservice.controller;

import com.blockchain.blockpulseservice.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionStreamController {
    private final NotificationService notificationService;

    public TransactionStreamController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTransactions() {
        return notificationService.subscribe();
    }
}

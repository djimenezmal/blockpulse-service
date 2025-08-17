package com.blockchain.blockpulseservice.controller;

import com.blockchain.blockpulseservice.model.dto.AnalyzedTransactionDTO;
import com.blockchain.blockpulseservice.service.AnalysisStream;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static reactor.core.publisher.BufferOverflowStrategy.DROP_OLDEST;

@RestController
@RequestMapping("/api/v1/transactions")
public class AnalysisController {
    private final AnalysisStream stream;

    public AnalysisController(AnalysisStream stream) {
        this.stream = stream;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AnalyzedTransactionDTO>> stream() {
        return stream.flux()
                .map(dto -> ServerSentEvent.builder(dto).build())
                .onBackpressureBuffer(32, DROP_OLDEST);
    }
}
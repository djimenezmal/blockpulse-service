package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.dto.AnalyzedTransactionDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Service
public class AnalysisStream {

    // Keeps only the most recent item; new subscribers get it immediately.
    private final Sinks.Many<AnalyzedTransactionDTO> sink = Sinks.many().replay().latest();

    /** Push a new snapshot (non-blocking). */
    public void publish(AnalyzedTransactionDTO dto) {
        sink.tryEmitNext(dto); // ignore backpressure; latest wins
    }

    /** Public flux for the controller. */
    public Flux<AnalyzedTransactionDTO> flux() {
        return sink.asFlux();
    }
}
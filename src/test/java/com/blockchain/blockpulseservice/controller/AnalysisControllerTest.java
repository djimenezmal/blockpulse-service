package com.blockchain.blockpulseservice.controller;

import com.blockchain.blockpulseservice.model.FeeClassification;
import com.blockchain.blockpulseservice.model.PatternType;
import com.blockchain.blockpulseservice.model.TransactionWindowSnapshotDTO;
import com.blockchain.blockpulseservice.model.dto.AnalyzedTransactionDTO;
import com.blockchain.blockpulseservice.service.AnalysisStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalysisControllerTest {

    private AnalysisStream analysisStream;
    private AnalysisController controller;

    @BeforeEach
    void setUp() {
        analysisStream = Mockito.mock(AnalysisStream.class);
        controller = new AnalysisController(analysisStream);
    }

    @Test
    void streamShouldReturnServerSentEventsFromStream() {
        AnalyzedTransactionDTO dto = new AnalyzedTransactionDTO(
                "id1",
                1,
                Instant.now(),
                BigDecimal.ONE,
                BigDecimal.TEN,
                123,
                Instant.now(),
                Set.of(PatternType.SURGE),
                FeeClassification.NORMAL,
                false,
                new TransactionWindowSnapshotDTO(0, BigDecimal.ZERO, BigDecimal.ZERO, 0)
        );
        when(analysisStream.flux()).thenReturn(Flux.just(dto));

        Flux<ServerSentEvent<AnalyzedTransactionDTO>> result = controller.stream();

        StepVerifier.create(result)
                .assertNext(event -> assertThat(event.data()).isEqualTo(dto))
                .verifyComplete();

        verify(analysisStream).flux();
    }
}


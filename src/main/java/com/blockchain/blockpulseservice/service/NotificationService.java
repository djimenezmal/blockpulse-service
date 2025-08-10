package com.blockchain.blockpulseservice.service;

import com.blockchain.blockpulseservice.model.AnalyzedTransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Broadcasts analyzed transactions to all subscribers using Server-Sent Events.
 */
@Slf4j
@Service
public class NotificationService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Registers a new subscriber and returns the {@link SseEmitter} handle.
     */
    public SseEmitter subscribe() {
        return subscribe(new SseEmitter(Long.MAX_VALUE));
    }

    /* package-private */ SseEmitter subscribe(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    public void sendAnalysis(AnalyzedTransactionDTO analyzedTransactionDTO) {
        for (var emitter : emitters) {
            Thread.startVirtualThread(() -> {
                try {
                    emitter.send(SseEmitter.event().data(analyzedTransactionDTO, MediaType.APPLICATION_JSON));
                    log.info("Sent transaction analysis message: {}", analyzedTransactionDTO);
                } catch (IOException e) {
                    emitters.remove(emitter);
                    emitter.completeWithError(e);
                }
            });
        }
    }
}

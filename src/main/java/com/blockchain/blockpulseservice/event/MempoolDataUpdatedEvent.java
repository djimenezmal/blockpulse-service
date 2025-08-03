package com.blockchain.blockpulseservice.event;

import com.blockchain.blockpulseservice.model.MempoolStats;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class MempoolDataUpdatedEvent extends ApplicationEvent {
    @Getter
    private final MempoolStats mempoolStats;

    public MempoolDataUpdatedEvent(Object source, MempoolStats mempoolStats) {
        super(source);
        this.mempoolStats = mempoolStats;
    }
}
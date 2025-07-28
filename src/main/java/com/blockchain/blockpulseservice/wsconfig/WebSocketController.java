package com.blockchain.blockpulseservice.wsconfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    
    //private final SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/subscribe/btc")
    @SendTo("/topic/transactions/btc")
    public void subscribeToBtcAnalysis() {
        log.info("Client subscribed to BTC analysis");
    }
}
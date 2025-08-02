package com.blockchain.blockpulseservice;

import com.blockchain.blockpulseservice.wsconfig.client.WebSocketReconnectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(WebSocketReconnectionProperties.class)
public class BlockPulseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlockPulseServiceApplication.class, args);
    }
}
package com.blockchain.blockpulseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlockPulseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlockPulseServiceApplication.class, args);
    }
}
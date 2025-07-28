package com.blockchain.blockpulseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlockpulseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlockpulseServiceApplication.class, args);
    }
}
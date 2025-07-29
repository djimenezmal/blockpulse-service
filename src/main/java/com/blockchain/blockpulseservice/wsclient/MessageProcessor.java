package com.blockchain.blockpulseservice.wsclient;

public interface MessageProcessor {
    void processMessage(String message) throws Exception;
}
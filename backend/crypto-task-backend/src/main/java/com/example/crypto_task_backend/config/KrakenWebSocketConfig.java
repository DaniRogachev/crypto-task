package com.example.crypto_task_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KrakenWebSocketConfig {
    
    @Value("${kraken.websocket.url:wss://ws.kraken.com}")
    private String krakenWebSocketUrl;
    
    @Value("${kraken.websocket.reconnect.delay:5000}")
    private int reconnectDelay;
    
    @Value("${kraken.rest.api.url:https://api.kraken.com/0}")
    private String krakenRestApiUrl;
    
    @Value("${kraken.top.pairs.count:20}")
    private int topPairsCount;
    
    @Value("${kraken.top.pairs.refresh.interval:1800000}")
    private long topPairsRefreshInterval;

    public String getKrakenWebSocketUrl() {
        return krakenWebSocketUrl;
    }

    public int getReconnectDelay() {
        return reconnectDelay;
    }
    
    public String getKrakenRestApiUrl() {
        return krakenRestApiUrl;
    }
    
    public int getTopPairsCount() {
        return topPairsCount;
    }
    
    public long getTopPairsRefreshInterval() {
        return topPairsRefreshInterval;
    }
}

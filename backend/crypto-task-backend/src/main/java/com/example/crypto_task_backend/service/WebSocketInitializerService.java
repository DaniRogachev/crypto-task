package com.example.crypto_task_backend.service;

import com.example.crypto_task_backend.service.websocket.CryptoWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

/**
 * Service to initialize WebSocket connections on application startup
 * Following Single Responsibility Principle
 */
@Service
public class WebSocketInitializerService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketInitializerService.class);
    
    private final CryptoWebSocketClient cryptoWebSocketClient;
    
    @Autowired
    public WebSocketInitializerService(CryptoWebSocketClient cryptoWebSocketClient) {
        this.cryptoWebSocketClient = cryptoWebSocketClient;
    }
    
    /**
     * Initialize WebSocket connections when the application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeWebSockets() {
        logger.info("Initializing WebSocket connections...");
        try {
            cryptoWebSocketClient.connect();
        } catch (Exception e) {
            logger.error("Failed to initialize WebSocket connections: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Clean up WebSocket connections on application shutdown
     */
    @PreDestroy
    public void cleanUp() {
        logger.info("Cleaning up WebSocket connections...");
        try {
            cryptoWebSocketClient.disconnect();
        } catch (Exception e) {
            logger.error("Error during WebSocket cleanup: {}", e.getMessage(), e);
        }
    }
}

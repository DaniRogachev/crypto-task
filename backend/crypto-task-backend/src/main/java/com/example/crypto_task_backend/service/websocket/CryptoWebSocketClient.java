package com.example.crypto_task_backend.service.websocket;

/**
 * Interface for crypto WebSocket clients
 * Following Interface Segregation Principle
 */
public interface CryptoWebSocketClient {
    
    /**
     * Connect to the WebSocket server
     */
    void connect();
    
    /**
     * Disconnect from the WebSocket server
     */
    void disconnect();
    
    /**
     * Subscribe to specific cryptocurrency pairs
     * @param pairs Array of cryptocurrency pairs to subscribe to
     */
    void subscribeToPairs(String[] pairs);
    
    /**
     * Check if the client is connected
     * @return true if connected, false otherwise
     */
    boolean isConnected();
}

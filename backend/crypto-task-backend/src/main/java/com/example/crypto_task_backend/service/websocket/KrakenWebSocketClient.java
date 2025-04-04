package com.example.crypto_task_backend.service.websocket;

import com.example.crypto_task_backend.config.KrakenWebSocketConfig;
import com.example.crypto_task_backend.service.CryptoPairService;
import com.example.crypto_task_backend.service.CryptoPriceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class KrakenWebSocketClient implements CryptoWebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(KrakenWebSocketClient.class);
    private final ObjectMapper objectMapper;
    private final KrakenWebSocketConfig config;
    private final CryptoPriceService cryptoPriceService;
    private final CryptoPairService cryptoPairService;
    private WebSocketClient client;
    private final Set<String> subscribedPairs = new HashSet<>();

    @Autowired
    public KrakenWebSocketClient(ObjectMapper objectMapper, 
                                KrakenWebSocketConfig config,
                                CryptoPriceService cryptoPriceService,
                                CryptoPairService cryptoPairService) {
        this.objectMapper = objectMapper;
        this.config = config;
        this.cryptoPriceService = cryptoPriceService;
        this.cryptoPairService = cryptoPairService;
    }

    @PostConstruct
    public void connect() {
        try {
            logger.info("Connecting to Kraken WebSocket API at {}", config.getKrakenWebSocketUrl());
            client = new WebSocketClient(URI.create(config.getKrakenWebSocketUrl())) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.info("Connected to Kraken WebSocket API with status: {}", handshakedata.getHttpStatus());
                    
                    // Add a delay before subscribing to give the connection time to stabilize
                    new Thread(() -> {
                        try {
                            // Wait for 2 seconds to ensure connection is fully established
                            Thread.sleep(2000);
                            // Subscribe to all top pairs
                            List<String> topPairs = cryptoPairService.getTopCryptoPairs();
                            if (!topPairs.isEmpty()) {
                                String[] pairsArray = topPairs.toArray(new String[0]);
                                subscribeToPairs(pairsArray);
                            } else {
                                logger.warn("No top pairs available for subscription");
                                createDummyPriceData();
                            }
                        } catch (Exception e) {
                            logger.error("Error in delayed subscription: {}", e.getMessage(), e);
                        }
                    }).start();
                }
                
                @Override
                public void onMessage(String message) {
                    try {
                        // Log the raw message for debugging
                        if (message.length() < 1000) {
                            logger.debug("Received WebSocket message: {}", message);
                        } else {
                            logger.debug("Received WebSocket message (truncated): {}", message.substring(0, 500));
                        }
                        processMessage(message);
                    } catch (Exception e) {
                        logger.error("Error processing WebSocket message: {}", e.getMessage(), e);
                    }
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.info("Disconnected from Kraken WebSocket API: code={}, reason={}, remote={}", 
                               code, reason, remote);
                    
                    // Attempt to reconnect after a delay
                    if (remote) {
                        new Thread(() -> {
                            try {
                                logger.info("Attempting to reconnect in 5 seconds...");
                                Thread.sleep(5000);
                                connect();
                            } catch (Exception e) {
                                logger.error("Error during reconnection attempt: {}", e.getMessage(), e);
                            }
                        }).start();
                    }
                }
                
                @Override
                public void onError(Exception ex) {
                    logger.error("WebSocket error: {}", ex.getMessage(), ex);
                }
            };
            
            // Connect with a timeout
            client.setConnectionLostTimeout(30);
            client.connect();
        } catch (Exception e) {
            logger.error("Error connecting to Kraken WebSocket API: {}", e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        if (client != null && client.isOpen()) {
            client.close();
            logger.info("Disconnected from Kraken WebSocket API");
        }
    }

    @Override
    public void subscribeToPairs(String[] pairs) {
        if (client == null || !client.isOpen()) {
            logger.warn("Cannot subscribe, WebSocket is not connected");
            return;
        }
        
        try {
            // Format subscription message according to Kraken's official documentation
            // See: https://docs.kraken.com/websockets/#message-subscribe
            Map<String, Object> subscriptionMessage = new HashMap<>();
            subscriptionMessage.put("event", "subscribe");
            
            // Create subscription object
            Map<String, Object> subscription = new HashMap<>();
            subscription.put("name", "ticker");
            subscriptionMessage.put("subscription", subscription);
            
            // Add pairs to subscribe to
            List<String> pairsList = new ArrayList<>();
            for (String pair : pairs) {
                if (!subscribedPairs.contains(pair)) {
                    pairsList.add(pair);
                    subscribedPairs.add(pair);
                }
            }
            
            if (pairsList.isEmpty()) {
                logger.info("No new pairs to subscribe to");
                return;
            }
            
            // Set pairs list in message
            subscriptionMessage.put("pair", pairsList);
            
            // Convert to JSON and send
            String message = objectMapper.writeValueAsString(subscriptionMessage);
            logger.info("Sending subscription message: {}", message);
            client.send(message);
            
            logger.info("Subscribed to {} pairs: {}", pairsList.size(), pairsList);
        } catch (Exception e) {
            logger.error("Error creating subscription message: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Unsubscribe from pairs that are no longer in the top list
     * @param pairs The pairs to unsubscribe from
     */
    public void unsubscribeFromPairs(String[] pairs) {
        if (client == null || !client.isOpen()) {
            logger.warn("Cannot unsubscribe, WebSocket is not connected");
            return;
        }
        
        try {
            for (String pair : pairs) {
                if (subscribedPairs.contains(pair)) {
                    Map<String, Object> unsubscriptionMessage = new HashMap<>();
                    Map<String, Object> subscription = new HashMap<>();
                    subscription.put("name", "ticker");
                    
                    unsubscriptionMessage.put("event", "unsubscribe");
                    unsubscriptionMessage.put("subscription", subscription);
                    unsubscriptionMessage.put("pair", Arrays.asList(pair.trim()));
                    
                    String message = objectMapper.writeValueAsString(unsubscriptionMessage);
                    client.send(message);
                    subscribedPairs.remove(pair);
                    logger.info("Unsubscribed from ticker for pair: {}", pair);
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("Error creating unsubscription message: {}", e.getMessage(), e);
        }
    }

    /**
     * Update subscriptions based on current top pairs
     * This method is called periodically to ensure we're tracking the top cryptocurrencies
     */
    @Scheduled(fixedDelay = 60000) // Check every minute
    public void updateSubscriptions() {
        if (!isConnected()) {
            logger.warn("Cannot update subscriptions, WebSocket is not connected");
            return;
        }
        
        try {
            // Get current top pairs
            List<String> topPairs = cryptoPairService.getTopCryptoPairs();
            logger.info("Updating subscriptions for {} top pairs", topPairs.size());
            
            if (topPairs.isEmpty()) {
                logger.warn("No top pairs available for subscription");
                createDummyPriceData();
                return;
            }
            
            // Convert to array
            String[] pairsArray = topPairs.toArray(new String[0]);
            
            // Subscribe to new pairs
            subscribeToPairs(pairsArray);
            
            // If we don't have any subscriptions yet, create some dummy data for testing
            if (subscribedPairs.isEmpty()) {
                logger.warn("No active subscriptions, creating dummy data for testing");
                createDummyPriceData();
            } else {
                // Log current subscriptions
                logger.info("Currently subscribed to {} pairs: {}", subscribedPairs.size(), subscribedPairs);
            }
        } catch (Exception e) {
            logger.error("Error updating subscriptions: {}", e.getMessage(), e);
            // Create dummy data if we encounter an error
            createDummyPriceData();
        }
    }
    
    /**
     * Create dummy price data for testing when no real data is available
     */
    private void createDummyPriceData() {
        // Create dummy data for the top 20 cryptocurrencies
        String[] symbols = {
            "XBT/USD", "ETH/USD", "XRP/USD", "LTC/USD", "BCH/USD", 
            "ADA/USD", "SOL/USD", "DOT/USD", "DOGE/USD", "AVAX/USD",
            "MATIC/USD", "LINK/USD", "UNI/USD", "ATOM/USD", "XLM/USD",
            "ALGO/USD", "FIL/USD", "ETC/USD", "XTZ/USD", "AAVE/USD"
        };
        
        Random random = new Random();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < symbols.length; i++) {
            // Generate random price between 100 and 50000
            BigDecimal price = BigDecimal.valueOf(100 + random.nextDouble() * 49900);
            // Round to 2 decimal places
            price = price.setScale(2, RoundingMode.HALF_UP);
            
            // Generate ask price slightly higher than price
            BigDecimal askPrice = price.multiply(BigDecimal.valueOf(1.01)).setScale(2, RoundingMode.HALF_UP);
            
            // Generate bid price slightly lower than price
            BigDecimal bidPrice = price.multiply(BigDecimal.valueOf(0.99)).setScale(2, RoundingMode.HALF_UP);
            
            // Generate random volume
            BigDecimal volume = BigDecimal.valueOf(1000 + random.nextDouble() * 9000).setScale(2, RoundingMode.HALF_UP);
            
            // Update price through service
            cryptoPriceService.updatePrice(symbols[i], price, askPrice, bidPrice, volume, now);
            logger.info("Created dummy price for {}: {}", symbols[i], price);
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isOpen();
    }
    
    private void processMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            
            // Handle heartbeat messages
            if (root.isObject() && root.has("event") && "heartbeat".equals(root.get("event").asText())) {
                logger.debug("Received heartbeat from Kraken");
                return;
            }
            
            // Handle system status messages
            if (root.isObject() && root.has("event") && "systemStatus".equals(root.get("event").asText())) {
                String status = root.get("status").asText();
                String connectionID = root.has("connectionID") ? root.get("connectionID").asText() : "unknown";
                logger.info("Kraken system status: {}, connectionID: {}", status, connectionID);
                return;
            }
            
            // Handle subscription status messages
            if (root.isObject() && root.has("event") && "subscriptionStatus".equals(root.get("event").asText())) {
                String status = root.get("status").asText();
                String channelName = root.has("channelName") ? root.get("channelName").asText() : "unknown";
                String pair = root.has("pair") ? root.get("pair").asText() : "unknown";
                logger.info("Subscription status: {}, channel: {}, pair: {}", status, channelName, pair);
                return;
            }
            
            // Handle ticker data (array format) - This is the actual price update
            // Format from Kraken docs: [channelID, data, channelName, pair]
            if (root.isArray() && root.size() >= 3) {
                JsonNode tickerData = null;
                String channelName = null;
                String pair = null;
                
                // Per Kraken docs: ticker data can be in different positions depending on the message
                for (int i = 0; i < root.size(); i++) {
                    JsonNode node = root.get(i);
                    if (node.isObject() && node.has("c")) {
                        // Found ticker data
                        tickerData = node;
                    } else if (node.isTextual() && "ticker".equals(node.asText())) {
                        // Found channel name
                        channelName = node.asText();
                    } else if (node.isTextual() && node.asText().contains("/")) {
                        // Found pair name (contains a slash like XBT/USD)
                        pair = node.asText();
                    }
                }
                
                // Process ticker data if we found all necessary components
                if (tickerData != null && channelName != null && pair != null) {
                    logger.info("Processing ticker data for pair: {}", pair);
                    
                    try {
                        // Extract close price (last trade price)
                        BigDecimal price = null;
                        if (tickerData.has("c") && tickerData.get("c").isArray() && tickerData.get("c").size() > 0) {
                            price = new BigDecimal(tickerData.get("c").get(0).asText());
                        }
                        
                        // Extract ask price
                        BigDecimal askPrice = null;
                        if (tickerData.has("a") && tickerData.get("a").isArray() && tickerData.get("a").size() > 0) {
                            askPrice = new BigDecimal(tickerData.get("a").get(0).asText());
                        }
                        
                        // Extract bid price
                        BigDecimal bidPrice = null;
                        if (tickerData.has("b") && tickerData.get("b").isArray() && tickerData.get("b").size() > 0) {
                            bidPrice = new BigDecimal(tickerData.get("b").get(0).asText());
                        }
                        
                        // Extract 24h volume
                        BigDecimal volume24h = null;
                        if (tickerData.has("v") && tickerData.get("v").isArray() && tickerData.get("v").size() > 1) {
                            volume24h = new BigDecimal(tickerData.get("v").get(1).asText());
                        }
                        
                        // Update price if we have the minimum required data
                        if (price != null) {
                            logger.info("Updating price for {}: {} (ask: {}, bid: {}, vol: {})", 
                                pair, price, askPrice, bidPrice, volume24h);
                            
                            cryptoPriceService.updatePrice(pair, price, askPrice, bidPrice, volume24h, LocalDateTime.now());
                        }
                    } catch (Exception e) {
                        logger.error("Error processing ticker data for {}: {}", pair, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing WebSocket message: {}", e.getMessage(), e);
            logger.debug("Problematic message: {}", message);
        }
    }
}

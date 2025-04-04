package com.example.crypto_task_backend.controller;

import com.example.crypto_task_backend.model.CryptoPrice;
import com.example.crypto_task_backend.service.CryptoPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CryptoWebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(CryptoWebSocketController.class);
    
    private final CryptoPriceService cryptoPriceService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public CryptoWebSocketController(CryptoPriceService cryptoPriceService, 
                                    SimpMessagingTemplate messagingTemplate) {
        this.cryptoPriceService = cryptoPriceService;
        this.messagingTemplate = messagingTemplate;
    }
    

    @MessageMapping("/subscribe-all")
    @SendTo("/topic/prices")
    public List<CryptoPrice> subscribeToAllPrices() {
        List<CryptoPrice> prices = cryptoPriceService.getAllPrices();
        logger.info("Client subscribed to all prices, sending {} prices", prices.size());
        return prices;
    }
    
    @MessageMapping("/subscribe/{symbol}")
    @SendTo("/topic/prices/{symbol}")
    public CryptoPrice subscribeToCryptocurrency(@DestinationVariable String symbol) {
        logger.info("Client subscribed to cryptocurrency: {}", symbol);
        return cryptoPriceService.getPriceBySymbol(symbol);
    }
    
    /**
     * Scheduled task to periodically send price updates to all clients
     * This ensures clients receive updates even if no price changes occur
     */
    @Scheduled(fixedRate = 5000)
    public void sendPeriodicUpdates() {
        List<CryptoPrice> prices = cryptoPriceService.getAllPrices();
        
        if (prices.isEmpty()) {
            logger.warn("No prices available to send in periodic update");
            
            // Create a dummy price for testing if no prices are available
            // This helps debug WebSocket connectivity issues
            if (prices.isEmpty()) {
                logger.info("Creating dummy test price for debugging");
                CryptoPrice dummyPrice = CryptoPrice.builder()
                    .symbol("TEST/USD")
                    .name("Test Currency")
                    .price(java.math.BigDecimal.valueOf(1000.0))
                    .build();
                messagingTemplate.convertAndSend("/topic/prices", List.of(dummyPrice));
            }
        } else {
            logger.info("Sending periodic update with {} prices", prices.size());
            messagingTemplate.convertAndSend("/topic/prices", prices);
        }
    }
}

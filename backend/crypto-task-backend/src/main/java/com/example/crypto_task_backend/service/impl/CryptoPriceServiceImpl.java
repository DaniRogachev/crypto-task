package com.example.crypto_task_backend.service.impl;

import com.example.crypto_task_backend.model.CryptoPrice;
import com.example.crypto_task_backend.service.CryptoPairService;
import com.example.crypto_task_backend.service.CryptoPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the CryptoPriceService
 * Follows Single Responsibility Principle by focusing only on price management
 * Uses in-memory storage for crypto prices instead of database
 */
@Service
public class CryptoPriceServiceImpl implements CryptoPriceService {
    private static final Logger logger = LoggerFactory.getLogger(CryptoPriceServiceImpl.class);
    
    // In-memory cache for quick access to latest prices
    private final Map<String, CryptoPrice> priceCache = new ConcurrentHashMap<>();
    
    private final SimpMessagingTemplate messagingTemplate;
    private final CryptoPairService cryptoPairService;

    @Autowired
    public CryptoPriceServiceImpl(SimpMessagingTemplate messagingTemplate,
                                 CryptoPairService cryptoPairService) {
        this.messagingTemplate = messagingTemplate;
        this.cryptoPairService = cryptoPairService;
    }

    @Override
    public List<CryptoPrice> getAllPrices() {
        List<CryptoPrice> prices = new ArrayList<>(priceCache.values());
        logger.info("Getting all prices from cache, found {} prices", prices.size());
        
        // Log the first few prices for debugging
        if (!prices.isEmpty()) {
            int limit = Math.min(3, prices.size());
            for (int i = 0; i < limit; i++) {
                CryptoPrice price = prices.get(i);
                logger.info("Sample price {}: {} = {}", i+1, price.getSymbol(), price.getPrice());
            }
        }
        
        return prices;
    }

    @Override
    public CryptoPrice getPriceBySymbol(String symbol) {
        CryptoPrice price = priceCache.get(symbol);
        if (price != null) {
            logger.debug("Found price for {} in cache: {}", symbol, price.getPrice());
        } else {
            logger.debug("Price for {} not found in cache", symbol);
        }
        return price;
    }

    @Override
    public CryptoPrice updatePrice(String symbol, BigDecimal price, BigDecimal askPrice, 
                                  BigDecimal bidPrice, BigDecimal volume24h, LocalDateTime lastUpdated) {
        try {
            // Create or update price data
            CryptoPrice cryptoPrice = priceCache.getOrDefault(symbol, 
                CryptoPrice.builder()
                    .symbol(symbol)
                    .name(cryptoPairService.getCryptoName(symbol))
                    .build());
            
            // Update price data
            cryptoPrice.setPrice(price);
            cryptoPrice.setAskPrice(askPrice);
            cryptoPrice.setBidPrice(bidPrice);
            cryptoPrice.setVolume24h(volume24h);
            cryptoPrice.setLastUpdated(lastUpdated);
            
            // Update cache
            priceCache.put(symbol, cryptoPrice);
            logger.info("Updated price in cache for {}: {}", symbol, price);
            
            // Send update to WebSocket clients for this specific symbol
            messagingTemplate.convertAndSend("/topic/prices/" + symbol, cryptoPrice);
            logger.debug("Sent price update for {} to WebSocket clients", symbol);
            
            // Periodically send full price list to all clients
            if (priceCache.size() % 5 == 0 || priceCache.size() <= 5) { // Send more frequently when we have few prices
                List<CryptoPrice> allPrices = new ArrayList<>(priceCache.values());
                messagingTemplate.convertAndSend("/topic/prices", allPrices);
                logger.info("Sent full price list ({} items) to WebSocket clients", allPrices.size());
            }
            
            return cryptoPrice;
        } catch (Exception e) {
            logger.error("Error updating price for {}: {}", symbol, e.getMessage(), e);
            throw e;
        }
    }
}

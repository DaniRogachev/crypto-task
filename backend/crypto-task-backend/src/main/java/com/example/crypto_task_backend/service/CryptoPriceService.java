package com.example.crypto_task_backend.service;

import com.example.crypto_task_backend.model.CryptoPrice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing cryptocurrency prices
 * Following Single Responsibility and Interface Segregation Principles
 */
public interface CryptoPriceService {
    
    List<CryptoPrice> getAllPrices();
    
    CryptoPrice getPriceBySymbol(String symbol);
    
    /**
     * Update price for a cryptocurrency
     * @param symbol The cryptocurrency symbol
     * @param price The current price
     * @param askPrice The current ask price
     * @param bidPrice The current bid price
     * @param volume24h The 24-hour trading volume
     * @param lastUpdated The timestamp of the update
     * @return The updated cryptocurrency price data
     */
    CryptoPrice updatePrice(String symbol, BigDecimal price, BigDecimal askPrice, 
                           BigDecimal bidPrice, BigDecimal volume24h, LocalDateTime lastUpdated);
}

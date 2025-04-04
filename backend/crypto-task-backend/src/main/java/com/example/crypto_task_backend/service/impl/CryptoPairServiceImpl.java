package com.example.crypto_task_backend.service.impl;

import com.example.crypto_task_backend.service.CryptoPairService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service implementation for managing cryptocurrency pairs
 * Uses a predefined list of top cryptocurrencies
 */
@Service
public class CryptoPairServiceImpl implements CryptoPairService {
    private static final Logger logger = LoggerFactory.getLogger(CryptoPairServiceImpl.class);
    
    // Cache of current top pairs
    private final List<String> topPairs = new ArrayList<>();
    
    // Map for cryptocurrency names
    private final Map<String, String> cryptoNames = new ConcurrentHashMap<>();
    
    // Common cryptocurrency name mappings
    private static final Map<String, String> COMMON_CRYPTO_NAMES = Map.ofEntries(
        Map.entry("XBT", "Bitcoin"),
        Map.entry("ETH", "Ethereum"),
        Map.entry("XRP", "Ripple"),
        Map.entry("LTC", "Litecoin"),
        Map.entry("BCH", "Bitcoin Cash"),
        Map.entry("ADA", "Cardano"),
        Map.entry("SOL", "Solana"),
        Map.entry("DOT", "Polkadot"),
        Map.entry("DOGE", "Dogecoin"),
        Map.entry("AVAX", "Avalanche"),
        Map.entry("MATIC", "Polygon"),
        Map.entry("LINK", "Chainlink"),
        Map.entry("UNI", "Uniswap"),
        Map.entry("ATOM", "Cosmos"),
        Map.entry("XLM", "Stellar"),
        Map.entry("ALGO", "Algorand"),
        Map.entry("FIL", "Filecoin"),
        Map.entry("ETC", "Ethereum Classic"),
        Map.entry("XTZ", "Tezos"),
        Map.entry("AAVE", "Aave")
    );

    public CryptoPairServiceImpl() {
        // Initialize with some default pairs in case the first API call fails
        this.topPairs.add("XBT/USD");
        this.topPairs.add("ETH/USD");
        
        // Initialize the name map
        COMMON_CRYPTO_NAMES.forEach((symbol, name) -> {
            cryptoNames.put(symbol + "/USD", name);
        });
        
        // Initial refresh of top pairs
        refreshTopCryptoPairs();
    }

    @Override
    public List<String> getTopCryptoPairs() {
        // Return a copy to prevent modification
        return new ArrayList<>(topPairs);
    }

    @Override
    @Scheduled(fixedDelayString = "${kraken.top.pairs.refresh.interval:1800000}")
    public List<String> refreshTopCryptoPairs() {
        try {
            logger.info("Refreshing top cryptocurrency pairs...");
            
            // Use Kraken's exact pair format for WebSocket API
            // Must use "wsname" format from their API (e.g., XBT/USD, not BTC/USD)
            List<String> predefinedPairs = Arrays.asList(
                "XBT/USD", // Bitcoin
                "ETH/USD", // Ethereum
                "XRP/USD", // Ripple
                "LTC/USD", // Litecoin
                "BCH/USD", // Bitcoin Cash
                "ADA/USD", // Cardano
                "SOL/USD", // Solana
                "DOT/USD", // Polkadot
                "DOGE/USD", // Dogecoin
                "AVAX/USD", // Avalanche
                "MATIC/USD", // Polygon
                "LINK/USD", // Chainlink
                "UNI/USD", // Uniswap
                "ATOM/USD", // Cosmos
                "XLM/USD", // Stellar
                "ALGO/USD", // Algorand
                "FIL/USD", // Filecoin
                "ETC/USD", // Ethereum Classic
                "XTZ/USD", // Tezos
                "AAVE/USD"  // Aave
            );
            
            // Update the top pairs list
            synchronized (topPairs) {
                topPairs.clear();
                topPairs.addAll(predefinedPairs);
            }
            
            // Update the name map for display purposes
            updateCryptoNamesMap();
            
            logger.info("Updated top {} cryptocurrency pairs", topPairs.size());
            return getTopCryptoPairs();
        } catch (Exception e) {
            logger.error("Error refreshing top cryptocurrency pairs: {}", e.getMessage(), e);
            return getTopCryptoPairs();
        }
    }
    
    /**
     * Update the mapping of cryptocurrency symbols to human-readable names
     */
    private void updateCryptoNamesMap() {
        // Map Kraken's pair format to human-readable names
        cryptoNames.put("XBT/USD", "Bitcoin");
        cryptoNames.put("ETH/USD", "Ethereum");
        cryptoNames.put("XRP/USD", "Ripple");
        cryptoNames.put("LTC/USD", "Litecoin");
        cryptoNames.put("BCH/USD", "Bitcoin Cash");
        cryptoNames.put("ADA/USD", "Cardano");
        cryptoNames.put("SOL/USD", "Solana");
        cryptoNames.put("DOT/USD", "Polkadot");
        cryptoNames.put("DOGE/USD", "Dogecoin");
        cryptoNames.put("AVAX/USD", "Avalanche");
        cryptoNames.put("MATIC/USD", "Polygon");
        cryptoNames.put("LINK/USD", "Chainlink");
        cryptoNames.put("UNI/USD", "Uniswap");
        cryptoNames.put("ATOM/USD", "Cosmos");
        cryptoNames.put("XLM/USD", "Stellar");
        cryptoNames.put("ALGO/USD", "Algorand");
        cryptoNames.put("FIL/USD", "Filecoin");
        cryptoNames.put("ETC/USD", "Ethereum Classic");
        cryptoNames.put("XTZ/USD", "Tezos");
        cryptoNames.put("AAVE/USD", "Aave");
    }

    @Override
    public String getCryptoName(String symbol) {
        // Check if we have a name for this symbol in our map
        if (cryptoNames.containsKey(symbol)) {
            return cryptoNames.get(symbol);
        }
        
        // For display purposes, try to extract a readable name from the symbol
        if (symbol.toUpperCase().endsWith("USD")) {
            String basePart = symbol;
            
            // Remove common prefixes and suffixes that Kraken uses
            if (basePart.startsWith("X")) {
                basePart = basePart.substring(1);
            }
            if (basePart.endsWith("ZUSD")) {
                basePart = basePart.substring(0, basePart.length() - 4);
            } else if (basePart.endsWith("USD")) {
                basePart = basePart.substring(0, basePart.length() - 3);
            }
            
            return basePart;
        }
        
        // If all else fails, just return the symbol as is
        return symbol;
    }
}

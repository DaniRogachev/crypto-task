package com.example.crypto_task_backend.controller;

import com.example.crypto_task_backend.model.CryptoPrice;
import com.example.crypto_task_backend.service.CryptoPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/crypto/prices")
public class CryptoPriceController {

    private static final Logger logger = LoggerFactory.getLogger(CryptoPriceController.class);
    private final CryptoPriceService cryptoPriceService;

    @Autowired
    public CryptoPriceController(CryptoPriceService cryptoPriceService) {
        this.cryptoPriceService = cryptoPriceService;
    }

    @GetMapping
    public ResponseEntity<List<CryptoPrice>> getAllPrices() {
        logger.info("API request: Get all cryptocurrency prices");
        List<CryptoPrice> prices = cryptoPriceService.getAllPrices();
        logger.info("Returning {} cryptocurrency prices", prices.size());
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<CryptoPrice> getPriceBySymbol(@PathVariable String symbol) {
        logger.info("API request: Get price for symbol: {}", symbol);
        CryptoPrice price = cryptoPriceService.getPriceBySymbol(symbol);
        if (price == null) {
            logger.warn("Price not found for symbol: {}", symbol);
            return ResponseEntity.notFound().build();
        }
        logger.info("Found price for {}: {}", symbol, price.getPrice());
        return ResponseEntity.ok(price);
    }

    @GetMapping("/value/{symbol}")
    public ResponseEntity<Double> getPriceValueBySymbol(@PathVariable String symbol) {
        logger.info("API request: Get price value for {}", symbol);
        CryptoPrice price = cryptoPriceService.getPriceBySymbol(symbol);
        if (price == null) {
            logger.warn("Price not found for {}", symbol);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(price.getPrice().doubleValue());
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Double>> getLatestPrices() {
        logger.info("API request: Get latest prices");
        List<CryptoPrice> prices = cryptoPriceService.getAllPrices();
        Map<String, Double> priceMap = prices.stream()
                .collect(Collectors.toMap(
                        CryptoPrice::getSymbol,
                        price -> price.getPrice().doubleValue()
                ));
        logger.info("Returning latest prices map with {} entries", priceMap.size());
        return ResponseEntity.ok(priceMap);
    }
}

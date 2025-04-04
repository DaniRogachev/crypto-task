package com.example.crypto_task_backend.controller;

import com.example.crypto_task_backend.service.CryptoPairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pairs")
@CrossOrigin(origins = "*")
public class CryptoPairController {
    
    private final CryptoPairService cryptoPairService;
    
    @Autowired
    public CryptoPairController(CryptoPairService cryptoPairService) {
        this.cryptoPairService = cryptoPairService;
    }
    
    /**
     * Get all top cryptocurrency pairs
     * @return List of cryptocurrency pairs (e.g., "XBT/USD")
     */
    @GetMapping
    public ResponseEntity<List<String>> getTopCryptoPairs() {
        return ResponseEntity.ok(cryptoPairService.getTopCryptoPairs());
    }
    
    /**
     * Get all top cryptocurrency pairs with their human-readable names
     * @return Map of cryptocurrency pairs to their names
     */
    @GetMapping("/with-names")
    public ResponseEntity<Map<String, String>> getTopCryptoPairsWithNames() {
        List<String> pairs = cryptoPairService.getTopCryptoPairs();
        Map<String, String> pairsWithNames = new HashMap<>();
        
        for (String pair : pairs) {
            pairsWithNames.put(pair, cryptoPairService.getCryptoName(pair));
        }
        
        return ResponseEntity.ok(pairsWithNames);
    }
    
    /**
     * Manually trigger a refresh of the top cryptocurrency pairs
     * @return The updated list of cryptocurrency pairs
     */
    @GetMapping("/refresh")
    public ResponseEntity<List<String>> refreshTopCryptoPairs() {
        return ResponseEntity.ok(cryptoPairService.refreshTopCryptoPairs());
    }
}

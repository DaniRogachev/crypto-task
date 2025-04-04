package com.example.crypto_task_backend.controller;

import com.example.crypto_task_backend.dto.TransactionRequest;
import com.example.crypto_task_backend.dto.TransactionResponse;
import com.example.crypto_task_backend.dto.UserHoldingResponse;
import com.example.crypto_task_backend.service.TransactionService;
import com.example.crypto_task_backend.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    private final UserService userService;

    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @PostMapping("/buy")
    public ResponseEntity<TransactionResponse> buyCrypto(@Valid @RequestBody TransactionRequest request) {
        logger.info("Buy request received for {} {}", request.getQuantity(), request.getSymbol());
        TransactionResponse response = transactionService.buyCrypto(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/sell")
    public ResponseEntity<TransactionResponse> sellCrypto(@Valid @RequestBody TransactionRequest request) {
        logger.info("Sell request received for {} {}", request.getQuantity(), request.getSymbol());
        TransactionResponse response = transactionService.sellCrypto(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getUserTransactions() {
        logger.info("Fetching user transactions");
        List<TransactionResponse> transactions = transactionService.getUserTransactions();
        logger.info("Returning {} transactions", transactions.size());
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/holdings")
    public ResponseEntity<List<UserHoldingResponse>> getUserHoldings() {
        logger.info("Fetching user crypto holdings");
        List<UserHoldingResponse> holdings = transactionService.getUserHoldings();
        logger.info("Returning {} holdings", holdings.size());
        return ResponseEntity.ok(holdings);
    }
    
    @GetMapping("/balance")
    public ResponseEntity<?> getUserBalance() {
        try {
            logger.info("Fetching user balance");
            BigDecimal balance = userService.getUserBalance();
            logger.info("Retrieved balance: {}", balance);
            return ResponseEntity.ok(Map.of("balance", balance));
        } catch (Exception e) {
            logger.error("Error fetching user balance: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Error fetching user balance: " + e.getMessage()));
        }
    }
    
    @PostMapping("/reset")
    public ResponseEntity<?> resetAccount() {
        try {
            logger.info("Account reset requested");
            transactionService.resetUserAccount();
            BigDecimal newBalance = userService.getUserBalance();
            logger.info("Account reset successful. New balance: {}", newBalance);
            return ResponseEntity.ok(Map.of(
                "message", "Account has been reset successfully",
                "balance", newBalance
            ));
        } catch (Exception e) {
            logger.error("Error resetting account: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Error resetting account: " + e.getMessage()));
        }
    }
}

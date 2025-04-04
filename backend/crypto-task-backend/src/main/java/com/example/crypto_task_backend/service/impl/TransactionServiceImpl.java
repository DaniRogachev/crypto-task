package com.example.crypto_task_backend.service.impl;

import com.example.crypto_task_backend.dto.TransactionRequest;
import com.example.crypto_task_backend.dto.TransactionResponse;
import com.example.crypto_task_backend.dto.UserHoldingResponse;
import com.example.crypto_task_backend.model.CryptoPrice;
import com.example.crypto_task_backend.model.Transaction;
import com.example.crypto_task_backend.model.User;
import com.example.crypto_task_backend.model.UserBalance;
import com.example.crypto_task_backend.repository.TransactionRepository;
import com.example.crypto_task_backend.service.CryptoPriceService;
import com.example.crypto_task_backend.service.TransactionService;
import com.example.crypto_task_backend.service.UserBalanceService;
import com.example.crypto_task_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final CryptoPriceService cryptoPriceService;
    private final UserBalanceService userBalanceService;

    @Autowired
    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            UserService userService,
            CryptoPriceService cryptoPriceService,
            UserBalanceService userBalanceService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.cryptoPriceService = cryptoPriceService;
        this.userBalanceService = userBalanceService;
    }

    @Override
    @Transactional
    public TransactionResponse buyCrypto(TransactionRequest request) {
        logger.info("Processing buy request for {} {}", request.getQuantity(), request.getSymbol());
        User user = userService.getCurrentUser();
        
        // Get the current price of the cryptocurrency
        var cryptoPrice = cryptoPriceService.getPriceBySymbol(request.getSymbol());
        if (cryptoPrice == null) {
            throw new RuntimeException("Crypto price not available for " + request.getSymbol());
        }
        BigDecimal currentPrice = cryptoPrice.getPrice();
        
        // Calculate the total cost of the purchase
        BigDecimal totalCost = currentPrice.multiply(request.getQuantity());
        
        // Verify the user has enough balance
        if (user.getBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Insufficient balance to complete this purchase");
        }
        
        // Update user's USD balance
        BigDecimal newBalance = user.getBalance().subtract(totalCost);
        userService.updateUserBalance(user.getId(), newBalance);
        
        // Update user's crypto balance
        userBalanceService.updateUserCryptoBalance(user, request.getSymbol(), request.getQuantity());
        
        // Create and save transaction record
        Transaction transaction = new Transaction(
                user,
                request.getSymbol(),
                Transaction.TransactionType.BUY,
                request.getQuantity(),
                currentPrice,
                totalCost
        );
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Buy transaction completed: ID={}, Amount={} {}, Total Cost=${}", 
                savedTransaction.getId(), request.getQuantity(), request.getSymbol(), totalCost);
        return new TransactionResponse(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponse sellCrypto(TransactionRequest request) {
        logger.info("Processing sell request for {} {}", request.getQuantity(), request.getSymbol());
        User user = userService.getCurrentUser();
        
        // Get the current price of the cryptocurrency
        CryptoPrice cryptoPrice = cryptoPriceService.getPriceBySymbol(request.getSymbol());
        if (cryptoPrice == null) {
            throw new RuntimeException("Crypto price not available for " + request.getSymbol());
        }
        BigDecimal currentPrice = cryptoPrice.getPrice();
        
        // Check if user has enough crypto balance to sell
        BigDecimal currentCryptoBalance = userBalanceService.getUserCryptoBalance(user, request.getSymbol());
        if (currentCryptoBalance.compareTo(request.getQuantity()) < 0) {
            throw new RuntimeException("Insufficient " + request.getSymbol() + " balance to complete this sale");
        }
        
        // Calculate the total value of the sale
        BigDecimal totalValue = currentPrice.multiply(request.getQuantity());
        
        // Update user's USD balance (add the sale value)
        BigDecimal newBalance = user.getBalance().add(totalValue);
        userService.updateUserBalance(user.getId(), newBalance);
        
        // Update user's crypto balance (subtract the sold amount)
        userBalanceService.updateUserCryptoBalance(user, request.getSymbol(), request.getQuantity().negate());
        
        // Create and save transaction record
        Transaction transaction = new Transaction(
                user,
                request.getSymbol(),
                Transaction.TransactionType.SELL,
                request.getQuantity(),
                currentPrice,
                totalValue
        );
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Sell transaction completed: ID={}, Amount={} {}, Total Value=${}", 
                savedTransaction.getId(), request.getQuantity(), request.getSymbol(), totalValue);
        return new TransactionResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions() {
        User user = userService.getCurrentUser();
        return transactionRepository.findByUserOrderByTransactionDateDesc(user).stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserHoldingResponse> getUserHoldings() {
        logger.info("Fetching user crypto holdings");
        User user = userService.getCurrentUser();
        List<UserBalance> balances = userBalanceService.getUserBalances(user);
        
        // Filter out zero balances
        balances = balances.stream()
                .filter(balance -> balance.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        
        List<UserHoldingResponse> holdings = new ArrayList<>();
        
        for (UserBalance balance : balances) {
            CryptoPrice cryptoPrice = cryptoPriceService.getPriceBySymbol(balance.getCryptoSymbol());
            if (cryptoPrice != null) {
                String cryptoName = cryptoPrice.getName() != null ? cryptoPrice.getName() : balance.getCryptoSymbol();
                holdings.add(new UserHoldingResponse(balance, cryptoName, cryptoPrice.getPrice()));
            } else {
                // If price not available, still show the holding but with zero price
                holdings.add(new UserHoldingResponse(balance, balance.getCryptoSymbol(), BigDecimal.ZERO));
            }
        }
        
        logger.info("Returning {} crypto holdings", holdings.size());
        return holdings;
    }
    
    @Override
    @Transactional
    public void resetUserAccount() {
        logger.info("Resetting user account");
        User user = userService.getCurrentUser();
        
        // Reset USD balance to initial value of $10,000
        BigDecimal initialBalance = new BigDecimal("10000.00");
        userService.updateUserBalance(user.getId(), initialBalance);
        logger.info("Reset balance to ${} for user ID {}", initialBalance, user.getId());
        
        // Delete all transactions
        List<Transaction> transactions = transactionRepository.findByUserOrderByTransactionDateDesc(user);
        if (!transactions.isEmpty()) {
            transactionRepository.deleteAll(transactions);
            logger.info("Deleted {} transactions", transactions.size());
        }
        
        // Clear all crypto balances
        List<UserBalance> balances = userBalanceService.getUserBalances(user);
        for (UserBalance balance : balances) {
            if (balance.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                userBalanceService.updateUserCryptoBalance(user, balance.getCryptoSymbol(), balance.getBalance().negate());
                logger.info("Reset balance for {} to zero", balance.getCryptoSymbol());
            }
        }
        
        logger.info("Account reset completed for user ID {}", user.getId());
    }
}

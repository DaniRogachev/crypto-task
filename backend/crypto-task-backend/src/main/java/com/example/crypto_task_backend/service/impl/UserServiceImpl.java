package com.example.crypto_task_backend.service.impl;

import com.example.crypto_task_backend.model.User;
import com.example.crypto_task_backend.repository.UserRepository;
import com.example.crypto_task_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        // Since we only have one user with no authentication, just get the first user 
        // or create a default one if none exists
        logger.info("Getting current user");
        List<User> users = userRepository.findAll();
        logger.info("Found {} users in the database", users.size());
        
        return users.stream()
                .findFirst()
                .orElseGet(() -> {
                    logger.info("No users found, creating default user");
                    User defaultUser = new User("defaultUser");
                    defaultUser.setBalance(new BigDecimal("10000.00"));
                    return userRepository.save(defaultUser);
                });
    }

    @Override
    @Transactional
    public User updateUserBalance(Long userId, BigDecimal newBalance) {
        logger.info("Updating balance for user ID {} to {}", userId, newBalance);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setBalance(newBalance);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getUserBalance() {
        logger.info("Getting user balance");
        User currentUser = getCurrentUser();
        BigDecimal balance = currentUser.getBalance();
        logger.info("User ID: {}, Balance: {}", currentUser.getId(), balance);
        return balance;
    }
}

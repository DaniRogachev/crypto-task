package com.example.crypto_task_backend.config;

import com.example.crypto_task_backend.model.User;
import com.example.crypto_task_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(@Autowired UserRepository userRepository) {
        return args -> {
            logger.info("Initializing default data");
            
            long userCount = userRepository.count();
            logger.info("Found {} users in database", userCount);
            
            if (userCount == 0) {
                logger.info("Creating default user with $10,000 balance");
                User defaultUser = new User("defaultUser");
                defaultUser.setBalance(new BigDecimal("10000.00"));
                User savedUser = userRepository.save(defaultUser);
                logger.info("Created default user with ID: {}", savedUser.getId());
            } else {
                logger.info("Default user already exists, skipping initialization");
                User existingUser = userRepository.findAll().get(0);
                logger.info("Using existing user: ID={}, username={}, balance={}", 
                        existingUser.getId(), existingUser.getUsername(), existingUser.getBalance());
            }
        };
    }
}

package com.example.crypto_task_backend.service;

import com.example.crypto_task_backend.model.User;
import java.math.BigDecimal;

public interface UserService {
    User getCurrentUser();
    User updateUserBalance(Long userId, BigDecimal newBalance);
    BigDecimal getUserBalance();
}

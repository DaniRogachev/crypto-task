package com.example.crypto_task_backend.service;

import com.example.crypto_task_backend.model.User;
import com.example.crypto_task_backend.model.UserBalance;

import java.math.BigDecimal;
import java.util.List;

public interface UserBalanceService {
    UserBalance updateUserCryptoBalance(User user, String cryptoSymbol, BigDecimal amount);
    List<UserBalance> getUserBalances(User user);
    BigDecimal getUserCryptoBalance(User user, String cryptoSymbol);
}

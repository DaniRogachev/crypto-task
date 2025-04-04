package com.example.crypto_task_backend.service.impl;

import com.example.crypto_task_backend.model.User;
import com.example.crypto_task_backend.model.UserBalance;
import com.example.crypto_task_backend.repository.UserBalanceRepository;
import com.example.crypto_task_backend.service.UserBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserBalanceServiceImpl implements UserBalanceService {

    private final UserBalanceRepository userBalanceRepository;

    @Autowired
    public UserBalanceServiceImpl(UserBalanceRepository userBalanceRepository) {
        this.userBalanceRepository = userBalanceRepository;
    }

    @Override
    @Transactional
    public UserBalance updateUserCryptoBalance(User user, String cryptoSymbol, BigDecimal amount) {
        UserBalance userBalance = userBalanceRepository.findByUserAndCryptoSymbol(user, cryptoSymbol)
                .orElse(new UserBalance(user, cryptoSymbol, BigDecimal.ZERO));
        
        BigDecimal newBalance = userBalance.getBalance().add(amount);
        userBalance.setBalance(newBalance);
        userBalance.setUpdatedAt(LocalDateTime.now());
        
        return userBalanceRepository.save(userBalance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBalance> getUserBalances(User user) {
        return userBalanceRepository.findByUserId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getUserCryptoBalance(User user, String cryptoSymbol) {
        return userBalanceRepository.findByUserAndCryptoSymbol(user, cryptoSymbol)
                .map(UserBalance::getBalance)
                .orElse(BigDecimal.ZERO);
    }
}

package com.example.crypto_task_backend.repository;

import com.example.crypto_task_backend.model.User;
import com.example.crypto_task_backend.model.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {
    List<UserBalance> findByUserId(Long userId);
    Optional<UserBalance> findByUserAndCryptoSymbol(User user, String cryptoSymbol);
}

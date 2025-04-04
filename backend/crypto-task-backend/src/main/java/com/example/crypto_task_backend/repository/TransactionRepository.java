package com.example.crypto_task_backend.repository;

import com.example.crypto_task_backend.model.Transaction;
import com.example.crypto_task_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);
}

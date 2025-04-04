package com.example.crypto_task_backend.service;

import com.example.crypto_task_backend.dto.TransactionRequest;
import com.example.crypto_task_backend.dto.TransactionResponse;
import com.example.crypto_task_backend.dto.UserHoldingResponse;

import java.util.List;

public interface TransactionService {
    TransactionResponse buyCrypto(TransactionRequest request);
    TransactionResponse sellCrypto(TransactionRequest request);
    List<TransactionResponse> getUserTransactions();
    List<UserHoldingResponse> getUserHoldings();
    void resetUserAccount();
}

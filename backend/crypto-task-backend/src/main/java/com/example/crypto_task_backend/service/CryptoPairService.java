package com.example.crypto_task_backend.service;

import java.util.List;

/**
 * Service interface for managing cryptocurrency pairs
 */
public interface CryptoPairService {
    

    List<String> getTopCryptoPairs();
  
    List<String> refreshTopCryptoPairs();

    String getCryptoName(String symbol);
}

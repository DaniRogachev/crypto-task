package com.example.crypto_task_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoPrice {
    private String symbol;
    private String name;
    private BigDecimal price;
    private BigDecimal askPrice;
    private BigDecimal bidPrice;
    private BigDecimal volume24h;
    private LocalDateTime lastUpdated;
}

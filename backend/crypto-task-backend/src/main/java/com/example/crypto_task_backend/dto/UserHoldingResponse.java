package com.example.crypto_task_backend.dto;

import com.example.crypto_task_backend.model.UserBalance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserHoldingResponse {
    private Long id;
    private String symbol;
    private String name;
    private BigDecimal balance;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private LocalDateTime lastUpdated;

    public UserHoldingResponse() {
    }

    public UserHoldingResponse(UserBalance userBalance, String cryptoName, BigDecimal currentPrice) {
        this.id = userBalance.getId();
        this.symbol = userBalance.getCryptoSymbol();
        this.name = cryptoName;
        this.balance = userBalance.getBalance();
        this.currentPrice = currentPrice;
        this.currentValue = balance.multiply(currentPrice);
        this.lastUpdated = userBalance.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

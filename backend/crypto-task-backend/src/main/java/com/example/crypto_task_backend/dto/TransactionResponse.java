package com.example.crypto_task_backend.dto;

import com.example.crypto_task_backend.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private String cryptoSymbol;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal price;
    private BigDecimal totalValue;
    private LocalDateTime transactionDate;

    public TransactionResponse() {
    }

    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.cryptoSymbol = transaction.getCryptoSymbol();
        this.transactionType = transaction.getTransactionType().name();
        this.amount = transaction.getAmount();
        this.price = transaction.getPrice();
        this.totalValue = transaction.getTotalValue();
        this.transactionDate = transaction.getTransactionDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCryptoSymbol() {
        return cryptoSymbol;
    }

    public void setCryptoSymbol(String cryptoSymbol) {
        this.cryptoSymbol = cryptoSymbol;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}

package com.bank_of_cli.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private long transactionID;
    private long accountID;
    private Long relatedAccountID;
    private String transactionType;
    private long amount;
    private LocalDateTime createdAT;

    public Transaction (long transactionID, long accountID, Long relatedAccountID, String transactionType, long amount, LocalDateTime createdAT) {
        this.transactionID = transactionID;
        this.accountID = accountID;
        this.relatedAccountID = relatedAccountID;
        this.transactionType = transactionType;
        this.amount = amount;
        this.createdAT = createdAT;
    }

    @Override
    public String toString() {
        return String.format("Transaction ID: %d | Type: %s | Amount: $%s | Date: %s", transactionID,
            transactionType, BigDecimal.valueOf(amount, 2).toPlainString(), createdAT);
    }

    public long getTransactionID() {
        return transactionID;
    }

    public long getAccountID() {
        return accountID;
    }

    public Long getRelatedAccountId() {
        return relatedAccountID;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public long getAmountCents() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAT;
    }
}
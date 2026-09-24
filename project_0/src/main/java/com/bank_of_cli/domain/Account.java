package com.bank_of_cli.domain;

import java.math.BigDecimal;

public class Account {
    private long accountID;
    private String pin;
    private long balance;

    public Account(long accountID, String pin, long balance) {
        this.accountID = accountID;
        this.pin = pin;
        this.balance = balance;
    }

    @Override 
    public String toString() {
        return String.format("Account ID: %d | Balance: $%s", accountID,
            BigDecimal.valueOf(balance, 2).toPlainString());
    }

    public long getAccountID() {
        return accountID;
    }

    public String getPin() {
        return pin;
    }

    public long getBalance() {
        return balance;
    }
}
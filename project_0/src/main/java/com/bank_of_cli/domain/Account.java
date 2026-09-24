package com.bank_of_cli.domain;

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
        return String.format("Account ID: %d | Balance: $%.2f", accountID, balance / 100.0);
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
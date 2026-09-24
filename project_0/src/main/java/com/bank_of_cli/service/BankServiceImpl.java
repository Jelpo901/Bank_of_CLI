package com.bank_of_cli.service;

import com.bank_of_cli.domain.Account;
import com.bank_of_cli.domain.Transaction;
import com.bank_of_cli.persistence.*;
import java.util.List;

public class BankServiceImpl implements BankService {
    private final BankDAO bankDAO;

    public BankServiceImpl(BankDAO bankDAO) {
        this.bankDAO = bankDAO;
    }

    @Override
    public void register (long accountID, String pin) {
        if (accountID <= 0) {
            throw new IllegalArgumentException("Account ID must be a positive number.");
        }
        if (bankDAO.getAccountByID(accountID) != null) {
            throw new IllegalArgumentException("Account ID already exists.");
        }
        validatePin(pin);
        Account account = new Account(accountID, pin, 0);
        bankDAO.addAccount(account);
    }

    @Override
    public Account login (long accountID, String pin) {
        Account account = bankDAO.getAccountByID(accountID);
        if (account == null || !account.getPin().equals(pin)) {
            throw new IllegalArgumentException("Invalid account ID or PIN.");
        }
        return account;
    }

    @Override
    public long getBalance (long accountID) {
        Account account = getExistingAccount(accountID);
        return account.getBalance();
    }

    @Override
    public void deposit (long accountID, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        getExistingAccount(accountID);
        bankDAO.deposit(accountID, amount);
    }

    @Override
    public void withdraw (long accountID, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        Account account = getExistingAccount(accountID);
        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds.");
        }
        boolean success = bankDAO.withdraw(accountID, amount);
        if (!success) {
            throw new IllegalStateException("Withdrawal failed due to an unexpected error.");
        }
    }

    @Override
    public void transfer (long fromAccountID, long toAccountID, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }
        if (fromAccountID == toAccountID) {
            throw new IllegalArgumentException("Cannot transfer to the same account.");
        }
        Account senderAccount = getExistingAccount(fromAccountID);
        getExistingAccount(toAccountID);
        if (senderAccount.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds for transfer.");
        }
        bankDAO.transfer(fromAccountID, toAccountID, amount);
    }

    @Override
    public List<Transaction> getTransactionHistory(long accountID) {
        getExistingAccount(accountID);
        return bankDAO.getRecentTransactions(accountID);
    }

    private Account getExistingAccount(long accountID) {
        Account account = bankDAO.getAccountByID(accountID);
        if (account == null) {
            throw new IllegalArgumentException("Account ID does not exist.");
        }
        return account;
    }

    private void validatePin(String pin) {
        if (pin == null || pin.length() != 4) {
            throw new IllegalArgumentException("PIN must be a 4-digit number.");
        }
        for (int i = 0; i < pin.length(); i++) {
            if (!Character.isDigit(pin.charAt(i))) {
                throw new IllegalArgumentException("PIN must be a 4-digit number.");
            }
        }
    }
}

package com.bank_of_cli.service;

import com.bank_of_cli.domain.Account;
import com.bank_of_cli.domain.Transaction;
import java.util.List;

public interface BankService {
    void register (long accoutnID, String pin);
    Account login (long accountID, String pin);
    long getBalance (long accountID);
    void deposit (long accountID, long amount);
    void withdraw (long accoutnID, long amount);
    void transfer (long fromAccountID, long toAccountID, long amount);
    List<Transaction> getTransactionHistory(long accountID);
}

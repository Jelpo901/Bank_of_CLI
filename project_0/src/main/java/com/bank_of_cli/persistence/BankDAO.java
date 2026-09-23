package com.bank_of_cli.persistence;

import java.util.List;
import com.bank_of_cli.domain.Account;
import com.bank_of_cli.domain.Transaction;

public interface BankDAO {
    void addAccount (Account account);
    Account getAccountByID (long accountID);
    void deposit (long accountID, long amount);
    boolean withdraw (long accoutnID, long amount);
    void transfer (long fromAccoutnID, long toAccoutnID, long amount);
    List<Transaction> getRecentTransactions(long accoutnID);
}

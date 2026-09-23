package com.bank_of_cli.persistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.bank_of_cli.domain.Account;
import com.bank_of_cli.domain.Transaction;

public class BankDAOImpl implements BankDAO {
    public static final String CREATE_ACCOUNT_TABLE = """
                CREATE TABLE IF NOT EXISTS accounts(
                    account_id BIGINT PRIMARY KEY,
                    pin VARCHAR(4) NOT NULL,
                    balance BIGINT NOT NULL DEFAULT 0,
                    CONSTRAINT check_balance_nonnegative CHECK (balance >= 0)
                )
                """;
}

package com.bank_of_cli.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.bank_of_cli.domain.Account;
import com.bank_of_cli.domain.Transaction;
import com.bank_of_cli.util.AppLogger;

public class BankDAOImpl implements BankDAO {
    private static final String CREATE_ACCOUNT_TABLE = """
            CREATE TABLE IF NOT EXISTS accounts(
                account_id BIGINT PRIMARY KEY,
                pin VARCHAR(4) NOT NULL,
                balance BIGINT NOT NULL DEFAULT 0,
                CONSTRAINT check_balance_nonnegative CHECK (balance >= 0)
            )
            """;
    private static final String CREATE_TRANSACTION_TABLE = """
            CREATE TABLE IF NOT EXISTS transactions(
                transaction_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                account_id BIGINT NOT NULL,
                related_account_id BIGINT,
                transaction_type VARCHAR(10) NOT NULL,
                amount BIGINT NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (account_id) REFERENCES accounts(account_id),
                CONSTRAINT check_amount_positive CHECK (amount > 0)
            )
            """;

    private static final String INSERT_ACCOUNT =
            "INSERT INTO accounts (account_id, pin, balance) VALUES (?, ?, ?)";
    private static final String FIND_ACCOUNT =
            "SELECT * FROM accounts WHERE account_id = ?";
    private static final String DEPOSIT =
            "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
    private static final String WITHDRAW =
            "UPDATE accounts SET balance = balance - ? WHERE account_id = ? AND balance >= ?";
    private static final String TRANSFER_HISTORY =
            "INSERT INTO transactions (account_id, related_account_id, transaction_type, amount) VALUES (?, ?, ?, ?)";
    private static final String GET_TRANSACTION_HISTORY =
            "SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";

    public BankDAOImpl() {
        initializeSchema();
    }

    @Override
    public void addAccount(Account account) {
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_ACCOUNT)) {
            statement.setLong(1, account.getAccountID());
            statement.setString(2, account.getPin());
            statement.setLong(3, account.getBalance());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("Error adding account", e);
        }
    }

    @Override
    public Account getAccountByID(long accountID) {
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ACCOUNT)) {
            statement.setLong(1, accountID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapAccount(resultSet) : null;
            }
        } catch (SQLException e) {
            throw databaseError("Error retrieving account", e);
        }
    }

    @Override
    public void deposit(long accountID, long amount) {
        updateBalance(accountID, amount, DEPOSIT, "DEPOSIT");
    }

    @Override
    public boolean withdraw(long accountID, long amount) {
        updateBalance(accountID, amount, WITHDRAW, "WITHDRAW");
        return true;
    }

    private void updateBalance(long accountID, long amount, String sql, String transactionType) {
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            try {
                connection.setAutoCommit(false);
                statement.setLong(1, amount);
                statement.setLong(2, accountID);
                if ("WITHDRAW".equals(transactionType)) {
                    statement.setLong(3, amount);
                }
                if (statement.executeUpdate() == 0) {
                    connection.rollback();
                    throw new IllegalArgumentException("Account not found or insufficient funds.");
                }
                addTransaction(connection, accountID, null, transactionType, amount);
                connection.commit();
            } catch (SQLException e) {
                rollback(connection, e);
                throw e;
            }
        } catch (SQLException e) {
            AppLogger.error("Database error while updating account balance", e);
            throw databaseError("Error updating account balance", e);
        }
    }

    @Override
    public void transfer(long fromAccountID, long toAccountID, long amount) {
        try (Connection connection = getConnection();
                PreparedStatement withdrawStatement = connection.prepareStatement(WITHDRAW);
                PreparedStatement depositStatement = connection.prepareStatement(DEPOSIT)) {
            try {
                connection.setAutoCommit(false);

                withdrawStatement.setLong(1, amount);
                withdrawStatement.setLong(2, fromAccountID);
                withdrawStatement.setLong(3, amount);
                if (withdrawStatement.executeUpdate() == 0) {
                    connection.rollback();
                    throw new IllegalArgumentException("Sender account not found or insufficient funds.");
                }

                depositStatement.setLong(1, amount);
                depositStatement.setLong(2, toAccountID);
                if (depositStatement.executeUpdate() == 0) {
                    connection.rollback();
                    throw new IllegalArgumentException("Receiver account not found.");
                }

                addTransaction(connection, fromAccountID, toAccountID, "TRANSFER_OUT", amount);
                addTransaction(connection, toAccountID, fromAccountID, "TRANSFER_IN", amount);
                connection.commit();
            } catch (SQLException e) {
                rollback(connection, e);
                throw e;
            }
        } catch (SQLException e) {
            AppLogger.error("Database error during transfer", e);
            throw databaseError("Error during transfer", e);
        }
    }

    @Override
    public List<Transaction> getRecentTransactions(long accountID) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_TRANSACTION_HISTORY)) {
            statement.setLong(1, accountID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapTransaction(resultSet));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw databaseError("Error retrieving transaction history", e);
        }
    }

    public void initializeSchema() {
        try (Connection connection = getConnection();
                PreparedStatement accountStatement = connection.prepareStatement(CREATE_ACCOUNT_TABLE);
                PreparedStatement transactionStatement = connection.prepareStatement(CREATE_TRANSACTION_TABLE)) {
            accountStatement.execute();
            transactionStatement.execute();
        } catch (SQLException e) {
            throw databaseError("Error initializing database schema", e);
        }
    }

    private void addTransaction(Connection connection, long accountID, Long relatedAccountID,
            String transactionType, long amount) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(TRANSFER_HISTORY)) {
            statement.setLong(1, accountID);
            if (relatedAccountID == null) {
                statement.setNull(2, Types.BIGINT);
            } else {
                statement.setLong(2, relatedAccountID);
            }
            statement.setString(3, transactionType);
            statement.setLong(4, amount);
            statement.executeUpdate();
        }
    }

    private Account mapAccount(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getLong("account_id"),
                resultSet.getString("pin"),
                resultSet.getLong("balance"));
    }

    private Transaction mapTransaction(ResultSet resultSet) throws SQLException {
        Long relatedAccountID = resultSet.getObject("related_account_id", Long.class);
        return new Transaction(
                resultSet.getLong("transaction_id"),
                resultSet.getLong("account_id"),
                relatedAccountID,
                resultSet.getString("transaction_type"),
                resultSet.getLong("amount"),
                resultSet.getTimestamp("timestamp").toLocalDateTime());
    }

    private Connection getConnection() {
        return ConnectionFactory.getConnectionFactory().getConnection();
    }

    private void rollback(Connection connection, SQLException cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }

    private IllegalStateException databaseError(String message, SQLException cause) {
        return new IllegalStateException(message, cause);
    }
}

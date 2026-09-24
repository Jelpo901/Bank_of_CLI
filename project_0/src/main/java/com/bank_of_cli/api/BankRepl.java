package com.bank_of_cli.api;

import com.bank_of_cli.domain.Account;
import com.bank_of_cli.domain.Transaction;
import com.bank_of_cli.service.BankService;
import com.bank_of_cli.util.AppLogger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Scanner;

public class BankRepl {
    private final BankService bankService;
    private final Scanner scanner = new Scanner(System.in);

    public BankRepl(BankService bankService) {
        this.bankService = bankService;
    }

    public void run() {
        System.out.println();
        System.out.println("================================");
        System.out.println("          Bank Of CLI           ");
        System.out.println("================================");

        while (true) {
            printMainMenu();

            try {
                int choice = readInt("Select an option: ");
                switch (choice) {
                    case 1:
                        registerAccount();
                        break;
                    case 2:
                        login();
                        break;
                    case 3: {
                        AppLogger.info("Exited Application");
                        System.out.println("Thank you for using Bank of CLI. Goodbye!");
                        return;
                    }
                    default:
                        System.out.println("Please select a valid option.");
                }
            } catch (Exception e) {
                AppLogger.error("System error: " + e.getMessage());
                System.out.println("Please try again later.");
            }
        }
    }

    private void registerAccount() {
        System.out.println();
        System.out.println("=== Register Account ===");
        long accountID = readLong("Enter account ID: ");
        String pin = readString("Enter a 4-digit PIN: ");
        bankService.register(accountID, pin);
        AppLogger.info("Account registered: " + accountID);
        System.out.println("Account registered successfully!");
    }

    private void login() {
        System.out.println();
        System.out.println("=== Login ===");
        long accountID = readLong("Enter account ID: ");
        String pin = readString("Enter PIN: ");
        Account account = bankService.login(accountID, pin);
        AppLogger.info("User logged in: " + accountID);
        System.out.println("Login successful!");
        runAccountMenu(account.getAccountID());
    }

    private void runAccountMenu(long accountID) {
        while (true) {
            printAccountMenu();
            try {
            int choice = readInt("Select an option: ");
            switch (choice) {
                case 1:
                    checkBalance(accountID);
                    break;
                case 2:
                    deposit(accountID);
                    break;
                case 3:
                    withdraw(accountID);
                    break;
                case 4:
                    transfer(accountID);
                    break;
                case 5:
                    viewTransactionHistory(accountID);
                    break;
                case 6:
                    AppLogger.info("User logged out: " + accountID);
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Please select a valid option.");
                }
            } catch (IllegalArgumentException e) {
                AppLogger.error("System error: " + e.getMessage());
                System.out.println("Please try again later.");
            }
        }
    }

    private void checkBalance(long accountID) {
        long balance = bankService.getBalance(accountID);
        AppLogger.info("Account viewed balance: " + accountID);
        System.out.println("Current balance: $" + formatAmount(balance));
    }

    private void deposit(long accountID) {
        System.out.println();
        System.out.println("=== Deposit ===");
        long amount = readAmount("Enter amount to deposit (e.g. 10.00): ");
        bankService.deposit(accountID, amount);
        AppLogger.info("Account deposited: " + accountID);
        System.out.println("Deposit successful!");
    }

    private void withdraw(long accountID) {
        System.out.println();
        System.out.println("=== Withdraw ===");
        long amount = readAmount("Enter amount to withdraw (e.g. 10.00): ");
        bankService.withdraw(accountID, amount);
        AppLogger.info("Account withdrew: " + accountID);
        System.out.println("Withdrawal successful!");
    }

    private void transfer(long accountID) {
        System.out.println();
        System.out.println("=== Transfer ===");
        long targetAccountID = readLong("Enter account ID to transfer to: ");
        long amount = readAmount("Enter amount to transfer (e.g. 10.00): ");
        bankService.transfer(accountID, targetAccountID, amount);
        AppLogger.info("Account transferred: " + accountID + " to " + targetAccountID);
        System.out.println("Transfer successful!");
    }

    private void viewTransactionHistory(long accountID) {
        System.out.println();
        System.out.println("=== Transaction History ===");
        List<Transaction> transactions = bankService.getTransactionHistory(accountID);
        AppLogger.info("Account viewed transaction history: " + accountID);
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            for (Transaction transaction : transactions) {
                System.out.println(transaction);
            }
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("1. Register Account");
        System.out.println("2. Login");
        System.out.println("3. Exit");
    }

    private void printAccountMenu() {
        System.out.println();
        System.out.println("================================");
        System.out.println("          Account Menu          ");
        System.out.println("================================");
        System.out.println("1. Check Balance");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. View Transaction History");
        System.out.println("6. Logout");
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid selection.");
        }
    }

    private long readLong(String prompt) {
        System.out.print(prompt);
        try {
            return Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid Account ID.");
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private long readAmount(String prompt) {
        System.out.print(prompt);
        try {
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim())
                    .setScale(2, RoundingMode.UNNECESSARY);
            if (amount.signum() <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }
            return amount.movePointRight(2).longValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("Please enter a valid amount.");
        }
    }

    private String formatAmount(long amountInCents) {
        return BigDecimal.valueOf(amountInCents, 2).setScale(2).toPlainString();
    }
}
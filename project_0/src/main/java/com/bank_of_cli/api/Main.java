package com.bank_of_cli.api;

import com.bank_of_cli.persistence.BankDAO;
import com.bank_of_cli.persistence.BankDAOImpl;
import com.bank_of_cli.service.BankService;
import com.bank_of_cli.service.BankServiceImpl;
import com.bank_of_cli.util.AppLogger;

public class Main {
    public static void main(String[] args) {
        try {
            BankDAO bankDAO = new BankDAOImpl();
            BankService bankService = new BankServiceImpl(bankDAO);
            new BankRepl(bankService).run();
        } catch (Exception e) {
            AppLogger.error("Error starting application: " + e.getMessage());
            System.out.println("Service is unavaiable. Please try again later.");
        }
    }
}
package com.bank_of_cli.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.bank_of_cli.domain.Account;
import com.bank_of_cli.persistence.BankDAO;

class BankServiceImplTest {
    private BankDAO bankDAO;
    private BankServiceImpl service;

    @BeforeEach
    void setUp() {
        bankDAO = org.mockito.Mockito.mock(BankDAO.class);
        service = new BankServiceImpl(bankDAO);
    }

    @Test
    void registerAddsNewAccount() {
        when(bankDAO.getAccountByID(12345L)).thenReturn(null);

        service.register(12345L, "1234");

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(bankDAO).addAccount(accountCaptor.capture());
        assertEquals(12345L, accountCaptor.getValue().getAccountID());
        assertEquals("1234", accountCaptor.getValue().getPin());
        assertEquals(0, accountCaptor.getValue().getBalance());
    }

    @Test
    void registerRejectsDuplicateAccount() {
        when(bankDAO.getAccountByID(12345L)).thenReturn(new Account(12345L, "1234", 0));

        assertThrows(IllegalArgumentException.class, () -> service.register(12345L, "1234"));
        verify(bankDAO, never()).addAccount(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void loginReturnsAccountWithCorrectPin() {
        Account account = new Account(12345L, "1234", 0);
        when(bankDAO.getAccountByID(12345L)).thenReturn(account);

        assertEquals(account, service.login(12345L, "1234"));
    }

    @Test
    void loginRejectsIncorrectPin() {
        when(bankDAO.getAccountByID(12345L)).thenReturn(new Account(12345L, "1234", 0));

        assertThrows(IllegalArgumentException.class, () -> service.login(12345L, "9999"));
    }

    @Test
    void getBalanceReturnsCurrentBalance() {
        when(bankDAO.getAccountByID(12345L)).thenReturn(new Account(12345L, "1234", 500));

        assertEquals(500, service.getBalance(12345L));
    }

    @Test
    void getBalanceRejectsMissingAccount() {
        when(bankDAO.getAccountByID(12345L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.getBalance(12345L));
    }

    @Test
    void depositAddsFunds() {
        when(bankDAO.getAccountByID(12345L)).thenReturn(new Account(12345L, "1234", 0));

        service.deposit(12345L, 100);

        verify(bankDAO).deposit(12345L, 100);
    }

    @Test
    void depositRejectsNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class, () -> service.deposit(12345L, 0));
        verify(bankDAO, never()).deposit(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void withdrawRemovesFunds() {
        when(bankDAO.getAccountByID(12345L)).thenReturn(new Account(12345L, "1234", 500));
        when(bankDAO.withdraw(12345L, 100)).thenReturn(true);

        service.withdraw(12345L, 100);

        verify(bankDAO).withdraw(12345L, 100);
    }

    @Test
    void withdrawRejectsOverdraft() {
        when(bankDAO.getAccountByID(12345L)).thenReturn(new Account(12345L, "1234", 50));

        assertThrows(IllegalArgumentException.class, () -> service.withdraw(12345L, 100));
        verify(bankDAO, never()).withdraw(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void transferMovesFundsBetweenAccounts() {
        when(bankDAO.getAccountByID(1L)).thenReturn(new Account(1L, "1234", 500));
        when(bankDAO.getAccountByID(2L)).thenReturn(new Account(2L, "1234", 100));

        service.transfer(1L, 2L, 100);

        verify(bankDAO).transfer(1L, 2L, 100);
    }

    @Test
    void transferRejectsSameAccount() {
        assertThrows(IllegalArgumentException.class, () -> service.transfer(1L, 1L, 100));
        verify(bankDAO, never()).transfer(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void getTransactionHistoryReturnsRepositoryResults() {
        Account account = new Account(12345L, "1234", 0);
        when(bankDAO.getAccountByID(12345L)).thenReturn(account);
        when(bankDAO.getRecentTransactions(12345L)).thenReturn(List.of());

        assertEquals(List.of(), service.getTransactionHistory(12345L));
    }

    @Test
    void getTransactionHistoryRejectsMissingAccount() {
        when(bankDAO.getAccountByID(12345L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.getTransactionHistory(12345L));
    }
}

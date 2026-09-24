package com.bank_of_cli.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bank_of_cli.domain.Account;
import com.bank_of_cli.persistence.BankDAO;

class BankServiceImplTest {
    private static final long ACCOUNT_ID = 12345L;
    private BankDAO bankDAO;
    private BankServiceImpl service;

    @BeforeEach
    void setUp() {
        bankDAO = org.mockito.Mockito.mock(BankDAO.class);
        service = new BankServiceImpl(bankDAO);
    }

    @Test
    void depositSucceedsForExistingAccount() {
        when(bankDAO.getAccountByID(ACCOUNT_ID)).thenReturn(new Account(ACCOUNT_ID, "1234", 0));

        service.deposit(ACCOUNT_ID, 12_00);

        verify(bankDAO).deposit(ACCOUNT_ID, 12_00);
    }

    @Test
    void withdrawFailsWhenAmountExceedsBalance() {
        when(bankDAO.getAccountByID(ACCOUNT_ID)).thenReturn(new Account(ACCOUNT_ID, "1234", 5_00));

        assertThrows(IllegalArgumentException.class, () -> service.withdraw(ACCOUNT_ID, 6_00));

        verify(bankDAO, never()).withdraw(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
    }
}

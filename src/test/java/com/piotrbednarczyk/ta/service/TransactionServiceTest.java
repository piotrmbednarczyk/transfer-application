package com.piotrbednarczyk.ta.service;

import com.piotrbednarczyk.ta.model.Account;
import com.piotrbednarczyk.ta.service.error.TransactionException;
import io.ebean.EbeanServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TransactionServiceTest {

    @Mock
    private EbeanServer server;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionService transactionService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNegativeAmount() throws Exception {
        transactionService.internalTransfer(1l, 2l, BigDecimal.TEN.negate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnZeroAmount() throws Exception {
        transactionService.internalTransfer(1l, 2l, BigDecimal.ZERO);
    }

    @Test(expected = TransactionException.class)
    public void shouldThrowExceptionOnNonExistentAccount() throws Exception {
        when(accountService.getAccount(1L)).thenReturn(empty());

        transactionService.internalTransfer(1l, 2l, BigDecimal.TEN);
    }

    @Test(expected = TransactionException.class)
    public void shouldThrowExceptionWhenAmountExceedsBalance() throws Exception {
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(9L));

        when(accountService.getAccount(1L)).thenReturn(of(account));

        transactionService.internalTransfer(1l, 2l, BigDecimal.TEN);
    }

    @Test
    public void shouldTransferAmount() throws Exception {
        Account fromAccount = new Account();
        fromAccount.setBalance(BigDecimal.TEN);

        Account toAccount = new Account();
        toAccount.setBalance(BigDecimal.ZERO);

        when(accountService.getAccount(1L)).thenReturn(of(fromAccount));
        when(accountService.getAccount(2L)).thenReturn(of(toAccount));

        transactionService.internalTransfer(1L, 2L, BigDecimal.TEN);

        verify(server).save(fromAccount);
        verify(server).save(toAccount);
        assertThat(fromAccount.getBalance(), comparesEqualTo(BigDecimal.ZERO));
        assertThat(toAccount.getBalance(), comparesEqualTo(BigDecimal.TEN));
    }
}
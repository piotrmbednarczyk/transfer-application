package com.piotrbednarczyk.ta.service;

import com.piotrbednarczyk.ta.model.Account;
import io.ebean.EbeanServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class AccountServiceTest {

    @Mock
    private EbeanServer server;

    @InjectMocks
    private AccountService accountService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldCreateAccount() {
        Account account = accountService.createAccount();

        assertThat(account, notNullValue());
        verify(server).insert(account);
    }

    @Test
    public void shouldDeleteAccount() {
        accountService.deleteAccount(1L);
        verify(server).delete(Account.class, 1L);
    }
}
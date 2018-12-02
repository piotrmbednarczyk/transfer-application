package com.piotrbednarczyk.ta.rest;

import com.piotrbednarczyk.ta.model.Account;
import com.piotrbednarczyk.ta.model.Transaction;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.text.MessageFormat.format;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AccountResourceTest extends RestApiTest {

    @Test
    public void shouldReturnEmptyAccountsList() {
        Response response = target.path("accounts").request().get();
        List<Account> accounts = getAccountsFromResponse(response);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        assertThat(accounts, is(empty()));
    }

    @Test
    public void shouldReturnObjectNotFoundStatus() {
        Response response = target.path(format("accounts/{0}", 1L)).request().get();
        assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }

    @Test
    public void shouldReturnSingleAccountFromDb() {
        Account account = new Account();
        ebeanServer.save(account);

        Response response = target.path(format("accounts/{0}", account.getId())).request().get();
        Account responseAccount = getEntityFromResponse(response, Account.class);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        assertThat(responseAccount, equalTo(account));
    }

    @Test
    public void shouldCreateAccountAndReturnInResponse() {
        Response response = target.path("accounts").request().post(null);
        Account account = getEntityFromResponse(response, Account.class);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        assertThat(account, notNullValue());
        assertThat(account.getId(), is(1L));
        assertThat(account.getCreationTime(), notNullValue());
        assertThat(account.getBalance(), is(BigDecimal.ZERO));

        assertThat(ebeanServer.find(Account.class, 1L), notNullValue());
    }

    @Test
    public void shouldDeleteAccount() {
        Account account = new Account();
        ebeanServer.save(account);

        Response response = target.path(format("accounts/{0}", account.getId())).request().delete();

        assertThat(response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
        assertThat(ebeanServer.find(Account.class, 1L), nullValue());
    }

    @Test
    public void shouldDeleteAccountWithTransactions() {
        Account account = createAccountWithTransactions();

        Response response = target.path(format("accounts/{0}", account.getId())).request().delete();

        assertThat(response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
        assertThat(ebeanServer.find(Account.class, 1L), nullValue());
        assertThat(ebeanServer.createQuery(Transaction.class)
                .where()
                .eq("account.id", 1L)
                .findList(), is(empty()));
    }

    @Test
    public void shouldReturnNoContentOnDeleteIfAccountNotExistent() {
        Response response = target.path(format("accounts/{0}", 1L)).request().delete();

        assertThat(response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
    }

    private List<Account> getAccountsFromResponse(Response response) {
        return getJsonb().fromJson(response.readEntity(String.class), new ArrayList<Account>(){}.getClass().getGenericSuperclass());
    }
}
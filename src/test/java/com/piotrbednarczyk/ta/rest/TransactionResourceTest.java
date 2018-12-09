package com.piotrbednarczyk.ta.rest;

import com.piotrbednarczyk.ta.model.Account;
import com.piotrbednarczyk.ta.model.Transaction;
import org.junit.Test;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.piotrbednarczyk.ta.model.Transaction.TransactionType.DEPOSIT;
import static com.piotrbednarczyk.ta.model.Transaction.TransactionType.WITHDRAWAL;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.text.MessageFormat.format;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TransactionResourceTest extends RestApiTest {

    @Test
    public void shouldReturnEmptyTransactionsList() {
        Response response = target.path("accounts/1/transactions").request().get();
        List<Transaction> transactions = getTransactionsFromResponse(response);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        assertThat(transactions, is(empty()));
    }

    @Test
    public void shouldReturnObjectNotFoundStatus() {
        Response response = target.path("accounts/1/transactions/1").request().get();
        assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }

    @Test
    public void shouldReturnTwoTransactionsForAccount() {
        Account account = createAccountWithTransactions();

        Response response = target.path("accounts/1/transactions").request().get();
        List<Transaction> transactions = getTransactionsFromResponse(response);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        assertThat(transactions, hasSize(2));
        assertThat(transactions, containsInAnyOrder(
                account.getTransactions().get(0),
                account.getTransactions().get(1)));

    }

    @Test
    public void shouldReturnTransactionForAccount() {
        Account account = createAccountWithTransactions();

        Response response = target.path(
                format("accounts/{0}/transactions/{1}",
                        account.getId(), account.getTransactions().get(0).getId()))
                .request().get();

        Transaction transaction = getEntityFromResponse(response, Transaction.class);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        assertThat(transaction, equalTo(account.getTransactions().get(0)));
    }

    @Test
    public void shouldDepositAmountToAccount() {
        Account account = createAccountWithTransactions();
        BigDecimal depositAmount = valueOf(100);

        Response response = target
                .path(format("accounts/{0}/transactions/deposit", account.getId()))
                .request()
                .post(form(new Form("amount", depositAmount.toPlainString())));

        Transaction transaction = getEntityFromResponse(response, Transaction.class);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        assertThat(transaction.getType(), equalTo(DEPOSIT));
        assertThat(transaction.getAmount(), comparesEqualTo(depositAmount));

        account = ebeanServer.find(Account.class, account.getId());
        assertThat(transaction, isIn(account.getTransactions()));
        assertThat(transaction.getAmount(), comparesEqualTo(account.getBalance()));
    }

    @Test
    public void shouldReturnNotEnoughFoundsMessage() {
        Account account = createAccountWithTransactions();
        BigDecimal depositAmount = valueOf(100);

        Response response = target
                .path(format("accounts/{0}/transactions/withdrawal", account.getId()))
                .request()
                .post(form(new Form("amount", depositAmount.toPlainString())));

        assertThat(response.getStatus(), equalTo(CONFLICT.getStatusCode()));
        assertThat(response.readEntity(String.class), startsWith("Not enough founds on account"));
    }

    @Test
    public void shouldReturnBadRequestOnNegativeAmount() {
        Account account = createAccountWithTransactions();
        BigDecimal depositAmount = valueOf(-1);

        Response response = target
                .path(format("accounts/{0}/transactions/withdrawal", account.getId()))
                .request()
                .post(form(new Form("amount", depositAmount.toPlainString())));

        assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
        assertThat(response.readEntity(String.class), equalTo("Non positive amounts are not accepted"));
    }

    @Test
    public void shouldWithdrawAmountFromAccount() {
        Account account = createAccountWithTransactions();
        BigDecimal depositAmount = valueOf(100);

        account.setBalance(depositAmount);
        ebeanServer.save(account);

        Response response = target
                .path(format("accounts/{0}/transactions/withdrawal", account.getId()))
                .request()
                .post(form(new Form("amount", depositAmount.toPlainString())));

        Transaction transaction = getEntityFromResponse(response, Transaction.class);
        account = ebeanServer.find(Account.class, account.getId());

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        assertThat(transaction.getType(), equalTo(WITHDRAWAL));
        assertThat(transaction.getAmount(), comparesEqualTo(depositAmount));
        assertThat(transaction, isIn(account.getTransactions()));
        assertThat(account.getBalance(), comparesEqualTo(ZERO));
    }

    @Test
    public void shouldTransferAmountBetweenAccounts() {
        Account fromAccount = createAccountWithTransactions();
        Account toAccount = createAccountWithTransactions();

        BigDecimal transferAmount = valueOf(100);
        fromAccount.setBalance(transferAmount);
        ebeanServer.save(fromAccount);

        Response response = target
                .path(format("accounts/{0}/transactions/transfer", fromAccount.getId()))
                .request()
                .post(form(new Form("amount", transferAmount.toPlainString())
                        .param("toAccountId", Long.toString(toAccount.getId()))));

        List<Transaction> transactions = getTransactionsFromResponse(response);
        fromAccount = ebeanServer.find(Account.class, fromAccount.getId());
        toAccount = ebeanServer.find(Account.class, toAccount.getId());

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        assertThat(transactions, hasSize(2));
        assertThat(fromAccount.getBalance(), comparesEqualTo(ZERO));
        assertThat(toAccount.getBalance(), comparesEqualTo(transferAmount));
    }
}

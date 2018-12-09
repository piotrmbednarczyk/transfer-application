package com.piotrbednarczyk.ta.rest;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.piotrbednarczyk.ta.Main;
import com.piotrbednarczyk.ta.model.Account;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

import static com.piotrbednarczyk.ta.Main.startServer;
import static java.math.BigDecimal.valueOf;
import static java.text.MessageFormat.format;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;

@RunWith(ConcurrentTestRunner.class)
public class ConcurrentTransactionResourceTest extends RestApiTest {

    private HttpServer server;
    private WebTarget target;
    private EbeanServer ebeanServer;

    private BigDecimal initialBalance1 = valueOf(100);
    private BigDecimal initialBalance2 = valueOf(200);
    private BigDecimal initialBalance3 = valueOf(300);

    private Account account1;
    private Account account2;
    private Account account3;

    @Before
    public void setUp() throws Exception {
        server = startServer();
        target = newClient().target(Main.BASE_URI);
        ebeanServer = Ebean.getDefaultServer();

        account1 = createAccount(initialBalance1);
        account2 = createAccount(initialBalance2);
        account3 = createAccount(initialBalance3);
    }

    @Test
    @ThreadCount(5)
    public void shouldTransferAllAmountsBetweenAccountsSoFinalBalancesAreEqualToInitialOnes() {
        transfer(valueOf(5), account3, account2);
        deposit(valueOf(1), account1);
        transfer(valueOf(20), account1, account3);
        withdrawal(valueOf(2), account2);
        transfer(valueOf(10), account2, account1);
        transfer(valueOf(5), account3, account2);
        withdrawal(valueOf(1), account1);
        transfer(valueOf(10), account3, account1);
        deposit(valueOf(2), account2);
    }

    private void transfer(BigDecimal transferAmount, Account fromAccount, Account toAccount) {
        Response response = target
                .path(format("accounts/{0}/transactions/transfer", fromAccount.getId()))
                .request()
                .post(form(new Form("amount", transferAmount.toPlainString())
                        .param("toAccountId", Long.toString(toAccount.getId()))));

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    private void deposit(BigDecimal depositAmount, Account account) {
        Response response = target
                .path(format("accounts/{0}/transactions/deposit", account.getId()))
                .request()
                .post(form(new Form("amount", depositAmount.toPlainString())));

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));

    }

    private void withdrawal(BigDecimal depositAmount, Account account) {
        Response response = target
                .path(format("accounts/{0}/transactions/withdrawal", account.getId()))
                .request()
                .post(form(new Form("amount", depositAmount.toPlainString())));

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    @After
    public void tearDown() throws Exception {
        account1 = ebeanServer.find(Account.class, account1.getId());
        account2 = ebeanServer.find(Account.class, account2.getId());
        account3 = ebeanServer.find(Account.class, account3.getId());

        assertThat(account1.getBalance(), comparesEqualTo(initialBalance1));
        assertThat(account2.getBalance(), comparesEqualTo(initialBalance2));
        assertThat(account3.getBalance(), comparesEqualTo(initialBalance3));

        server.shutdown();
    }

    private Account createAccount(BigDecimal initialBalance) {
        Account account = new Account();
        account.setBalance(initialBalance);
        ebeanServer.save(account);
        return account;
    }
}

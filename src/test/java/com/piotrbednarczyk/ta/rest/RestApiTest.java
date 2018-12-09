package com.piotrbednarczyk.ta.rest;

import com.piotrbednarczyk.ta.Main;
import com.piotrbednarczyk.ta.model.Account;
import com.piotrbednarczyk.ta.model.Transaction;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyVisibilityStrategy;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.piotrbednarczyk.ta.Main.startServer;
import static com.piotrbednarczyk.ta.model.Transaction.TransactionType.DEPOSIT;
import static com.piotrbednarczyk.ta.model.Transaction.TransactionType.WITHDRAWAL;
import static javax.ws.rs.client.ClientBuilder.newClient;

public abstract class RestApiTest {

    protected HttpServer server;
    protected WebTarget target;
    protected EbeanServer ebeanServer;

    @Before
    public void setUp() throws Exception {
        server = startServer();
        target = newClient().target(Main.BASE_URI);
        ebeanServer = Ebean.getDefaultServer();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    public  <T> T getEntityFromResponse(Response response, Class<T> clazz) {
        return getJsonb().fromJson(response.readEntity(String.class), clazz);
    }

    public Jsonb getJsonb() {
        return JsonbBuilder.create(new JsonbConfig().withPropertyVisibilityStrategy(
                new PropertyVisibilityStrategy() {
                    @Override
                    public boolean isVisible(Field field) {
                        return true;
                    }

                    @Override
                    public boolean isVisible(Method method) {
                        return true;
                    }
                }));
    }

    public List<Transaction> getTransactionsFromResponse(Response response) {
        return getJsonb().fromJson(response.readEntity(String.class), new ArrayList<Transaction>(){}.getClass().getGenericSuperclass());
    }


    public Account createAccountWithTransactions() {
        Account account = new Account();

        Transaction deposit = new Transaction();
        deposit.setAccount(account);
        deposit.setAmount(BigDecimal.TEN);
        deposit.setType(DEPOSIT);
        account.getTransactions().add(deposit);


        Transaction withdrawal = new Transaction();
        withdrawal.setAccount(account);
        withdrawal.setAmount(BigDecimal.TEN);
        withdrawal.setType(WITHDRAWAL);
        account.getTransactions().add(withdrawal);

        ebeanServer.save(account);
        return account;
    }
}

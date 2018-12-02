package com.piotrbednarczyk.ta.service;

import com.piotrbednarczyk.ta.model.Account;
import io.ebean.EbeanServer;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

public class AccountService {

    private static final Logger LOG = getLogger(AccountService.class);

    private EbeanServer server;

    @Inject
    public AccountService(EbeanServer server) {
        this.server = server;
    }

    public Account createAccount() {
        LOG.debug("createAccount");
        Account account = new Account();
        server.insert(account);
        return account;
    }

    public List<Account> getAccounts() {
        LOG.debug("getAccounts");
        return server.find(Account.class)
                .filterMany("transactions")
                .findList();
    }

    public Optional<Account> getAccount(long id) {
        LOG.debug("getAccount {}", id);
        return ofNullable(server.find(Account.class).where().idEq(id).findOne());
    }

    public void deleteAccount(long id) {
        LOG.debug("deleteAccount {}", id);
        server.delete(Account.class, id);
    }
}

package com.piotrbednarczyk.ta.service;

import com.piotrbednarczyk.ta.model.Account;
import com.piotrbednarczyk.ta.model.Transaction;
import com.piotrbednarczyk.ta.service.error.TransactionException;
import io.ebean.EbeanServer;
import io.ebean.annotation.Transactional;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.piotrbednarczyk.ta.model.Transaction.TransactionType.DEPOSIT;
import static com.piotrbednarczyk.ta.model.Transaction.TransactionType.WITHDRAWAL;
import static io.ebean.annotation.TxIsolation.SERIALIZABLE;
import static java.math.RoundingMode.DOWN;
import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

public class TransactionService {

    private static final Logger LOG = getLogger(TransactionService.class);

    private EbeanServer server;

    private AccountService accountService;

    @Inject
    public TransactionService(EbeanServer server, AccountService accountService) {
        this.server = server;
        this.accountService = accountService;
    }

    public List<Transaction> getTransactionsForAccount(Long accountId) {
        LOG.debug("getTransactionsForAccount {}", accountId);
        return server.createQuery(Transaction.class)
                .where()
                .eq("account.id", accountId)
                .findList();
    }

    public Optional<Transaction> getTransaction(Long id) {
        LOG.debug("getTransaction {}", id);
        return ofNullable(server.find(Transaction.class, id));
    }

    @Transactional(isolation = SERIALIZABLE)
    public Transaction deposit(Long accountId, BigDecimal amount) {
        return internalDeposit(accountId, amount);

    }

    protected Transaction internalDeposit(Long accountId, BigDecimal amount) {
        LOG.debug("deposit {} to account {}", amount, accountId);

        amount = checkAndNormalizeAmount(amount);
        Account account = getAccountForUpdate(accountId);
        account.setBalance(account.getBalance().add(amount));

        Transaction transaction = createTransaction(amount, account, DEPOSIT);
        account.getTransactions().add(transaction);
        server.save(account);

        return transaction;
    }

    @Transactional(isolation = SERIALIZABLE)
    public Transaction withdrawal(Long accountId, BigDecimal amount) {
        return internalWithdrawal(accountId, amount);
    }

    protected Transaction internalWithdrawal(Long accountId, BigDecimal amount) {
        LOG.debug("withdrawal {} from account {}", amount, accountId);

        amount = checkAndNormalizeAmount(amount);
        Account account = getAccountForUpdate(accountId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new TransactionException(
                    format("Not enough founds on account {0}. Available: ({1})",
                            accountId, account.getBalance()));
        }

        account.setBalance(account.getBalance().subtract(amount));

        Transaction transaction = createTransaction(amount, account, WITHDRAWAL);
        account.getTransactions().add(transaction);
        server.save(account);

        return transaction;
    }

    @Transactional(isolation = SERIALIZABLE)
    public List<Transaction> transfer(Long fromId, Long toId, BigDecimal amount) {
        return internalTransfer(fromId, toId, amount);
     }

    protected List<Transaction> internalTransfer(Long fromId, Long toId, BigDecimal amount) {
        LOG.debug("transfer {} from account {} to account {}", amount, fromId, toId);
        checkArgument(fromId != toId, "source account and destination accounts should be different");

        List<Transaction> transactions = new ArrayList<>();

        if (fromId < toId) {
            transactions.add(internalWithdrawal(fromId, amount));
            transactions.add(internalDeposit(toId, amount));
        } else {
            transactions.add(internalDeposit(toId, amount));
            transactions.add(internalWithdrawal(fromId, amount));
        }

        return transactions;
    }

    private Transaction createTransaction(BigDecimal amount, Account account, Transaction.TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAccount(account);
        transaction.setAmount(amount);
        return transaction;
    }

    private Account getAccountForUpdate(Long accountId) {
        return accountService.getAccountForUpdate(accountId)
                .orElseThrow(() -> new TransactionException(
                        format("Account {0} not found", accountId)));
    }

    private BigDecimal checkAndNormalizeAmount(BigDecimal amount) {
        checkArgument(amount.signum() == 1, "Non positive amounts are not accepted");
        return amount.setScale(2, DOWN);
    }
}

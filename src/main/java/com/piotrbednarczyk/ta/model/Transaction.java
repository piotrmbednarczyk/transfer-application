package com.piotrbednarczyk.ta.model;

import io.ebean.annotation.SoftDelete;
import io.ebean.annotation.WhenCreated;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

import static org.glassfish.jersey.linking.InjectLink.Style.ABSOLUTE;

@Entity
@InjectLinks({@InjectLink(value="accounts/${instance.account.id}/transactions/${instance.id}", rel="self", style = ABSOLUTE),
        @InjectLink(value="accounts/${instance.account.id}/", rel="account", style = ABSOLUTE)})
public class Transaction {

    public enum TransactionType {

        DEPOSIT,

        WITHDRAWAL
    }

    @Id
    private long id;

    @Enumerated
    TransactionType type;

    @ManyToOne(optional = false)
    @JsonbTransient
    private Account account;

    @WhenCreated
    private Instant creationTime;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @SoftDelete
    private boolean deleted;

    public long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}

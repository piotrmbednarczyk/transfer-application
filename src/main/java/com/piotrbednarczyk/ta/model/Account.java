package com.piotrbednarczyk.ta.model;

import io.ebean.annotation.SoftDelete;
import io.ebean.annotation.WhenCreated;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.glassfish.jersey.linking.InjectLink.Style.ABSOLUTE;

@Entity
@InjectLinks({@InjectLink(value="accounts/${instance.id}", rel="self", style = ABSOLUTE),
        @InjectLink(value="accounts/${instance.id}/transactions", rel="transactions", style = ABSOLUTE)})
public class Account {

    @Id
    private long id;

    @WhenCreated
    private Instant creationTime;

    @Column(precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonbTransient
    private List<Transaction> transactions = new ArrayList<>();

    @SoftDelete
    private boolean deleted;

    public long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
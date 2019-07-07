package com.vrq.revolut.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;


@Entity
@Table(name = "TRANSFERS")
public class Transfer {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @JsonProperty
    private long id;

    @Column(name = "AMOUNT",  nullable = false)
    @NotNull
    @JsonProperty
    private BigDecimal amount;

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    @JsonProperty
    @JsonIgnoreProperties({"transfersFrom", "transfersTo"})
    private Account fromAccount;

    public BigDecimal getAmount() {
        return amount;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    @JsonProperty
    @JsonIgnoreProperties({"transfersFrom", "transfersTo"})
    private Account toAccount;

    public Transfer() {
        // Jackson deserialization
    }

    public Transfer(BigDecimal amount, Account fromAccount, Account toAccount) {
        this.amount = amount;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }
}
package com.vrq.revolut.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;


@Entity
@Table(name = "transfers")
public class Transfer {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private long id;

    @Column(name = "amount", nullable = false)
    @NotNull
    @JsonProperty
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    @JsonProperty
    @JsonIgnoreProperties({"transfersFrom", "transfersTo", "balance"})
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    @JsonProperty
    @JsonIgnoreProperties({"transfersFrom", "transfersTo", "balance"})
    private Account toAccount;

    public Transfer() {
        // Jackson deserialization
    }

    public Transfer(BigDecimal amount, Account fromAccount, Account toAccount) {
        this.amount = amount;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    public Transfer(Long id, BigDecimal amount, Account fromAccount, Account toAccount) {
        this.id = id;
        this.amount = amount;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
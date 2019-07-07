package com.vrq.revolut.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


@Entity
@Table(name = "ACCOUNTS")
public class Account {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id == account.id &&
                Objects.equals(balance, account.balance) &&
                Objects.equals(transfersFrom, account.transfersFrom) &&
                Objects.equals(transfersTo, account.transfersTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, balance, transfersFrom, transfersTo);
    }

    public long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private long id;

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Column(name = "BALANCE", nullable = false)
    @NotNull
    @JsonProperty
    private BigDecimal balance = BigDecimal.valueOf(500000);

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fromAccount")
    @JsonIgnoreProperties({"fromAccount", "toAccount"})
    @JsonProperty
    private List<Transfer> transfersFrom;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "toAccount")
    @JsonIgnoreProperties({"fromAccount", "toAccount"})
    @JsonProperty
    private List<Transfer> transfersTo;

    public Account() {
        // Jackson deserialization
    }

    public Account(long id) {
        this.id = id;
    }
}
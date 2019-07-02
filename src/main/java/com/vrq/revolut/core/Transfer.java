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

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    @JsonProperty
    @JsonIgnoreProperties({"transfersFrom", "transfersTo"})
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    @JsonProperty
    @JsonIgnoreProperties({"transfersFrom", "transfersTo"})
    private Account toAccount;

    public Transfer() {
        // Jackson deserialization
    }

    public Transfer(long id, BigDecimal amount, Account fromAccount, Account toAccount) {
        this.id = id;
        this.amount = amount;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    @JsonProperty
    public long getId() {
        return id;
    }

}
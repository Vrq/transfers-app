package com.vrq.revolut.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;


@Entity
@Table(name = "ACCOUNTS")
public class Account {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private long id;

    @Column(name = "NAME", nullable = false)
    @NotNull
    @JsonProperty
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fromAccount")
    @JsonIgnoreProperties({"fromAccount", "toAccount"})
    private List<Transfer> transfersFrom;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "toAccount")
    @JsonIgnoreProperties({"fromAccount", "toAccount"})
    private List<Transfer> transfersTo;

    public Account() {
        // Jackson deserialization
    }

    public Account(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @JsonProperty
    public long getId() {
        return id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public List<Transfer> getTransfersFrom() {
        return transfersFrom;
    }

    @JsonProperty
    public List<Transfer> getTransfersTo() {
        return transfersTo;
    }
}
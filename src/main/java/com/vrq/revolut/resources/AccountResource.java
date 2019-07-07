package com.vrq.revolut.resources;


import com.vrq.revolut.core.Account;
import com.vrq.revolut.db.AccountDao;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.List;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {

    private final AccountDao accountDao;

    public AccountResource(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @POST
    @UnitOfWork
    public Account create() {
        return accountDao.create();
    }

    @GET
    @UnitOfWork
    public List<Account> getAll(){
        return accountDao.getAll();
    }

    @GET
    @Path("/{id}")
    @UnitOfWork
    public Account getById(@PathParam("id") long id){
        return accountDao.findById(id);
    }

    @POST
    @Path("/{id}/deposit/{amount}")
    @UnitOfWork
    public Account deposit(@PathParam("id") long accountId, @PathParam("amount")BigDecimal depositAmount){
        return accountDao.deposit(accountId, depositAmount);
    }
}
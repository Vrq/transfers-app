package com.vrq.revolut.resources;


import com.vrq.revolut.core.Account;
import com.vrq.revolut.db.AccountDao;
import io.dropwizard.hibernate.UnitOfWork;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
    public Account add(@Valid Account account) {
        return accountDao.create(account);
    }

    @GET
    @UnitOfWork
    public List<Account> getAll(){
        return accountDao.getAll();
    }
}
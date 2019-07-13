package com.vrq.revolut.resources;


import com.vrq.revolut.core.Account;
import com.vrq.revolut.service.AccountService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.List;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {
    private final AccountService accountService;

    public AccountResource(AccountService accountService) {
        this.accountService = accountService;
    }

    @POST
    public Account create() {
        return accountService.createAccount();
    }

    @GET
    public List<Account> getAll(){
        return accountService.getAllAccounts();
    }

    @GET
    @Path("/{id}")
    public Account getById(@PathParam("id") long id){
        return accountService.findAccountById(id);
    }

    @POST
    @Path("/{id}/deposit/{amount}")
    public Account deposit(@PathParam("id") long accountId, @PathParam("amount")BigDecimal depositAmount){
        return accountService.depositToAccount(accountId, depositAmount);
    }
}
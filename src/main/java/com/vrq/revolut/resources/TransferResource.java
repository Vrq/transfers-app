package com.vrq.revolut.resources;


import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import com.vrq.revolut.db.AccountDao;
import com.vrq.revolut.db.TransferDao;
import io.dropwizard.hibernate.UnitOfWork;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;

@Path("/transfers")
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {

    private final TransferDao transferDao;
    private final AccountDao accountDao;

    public TransferResource(TransferDao transferDao, AccountDao accountDao) {
        this.transferDao = transferDao;
        this.accountDao = accountDao;
    }

    @POST
    @UnitOfWork
    public Transfer add(@Valid Transfer transfer) {
        if(transfer.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new WebApplicationException("Cannot make transfers less than 0",Response.Status.BAD_REQUEST);
        }
        if(transfer.getFromAccount() == null || transfer.getToAccount() == null) {
            throw new WebApplicationException("Transfer recipients not specified correctly",Response.Status.BAD_REQUEST);

        }
        Account fromAccount = accountDao.findById(transfer.getFromAccount().getId());
        Account toAccount = accountDao.findById(transfer.getToAccount().getId());
        if(fromAccount.getBalance().compareTo(transfer.getAmount())< 0) {
            throw new WebApplicationException("Insufficient balance of the sender account",Response.Status.BAD_REQUEST);


        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(transfer.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transfer.getAmount()));
        accountDao.update(fromAccount);
        accountDao.update(toAccount);
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        return transferDao.create(transfer);
    }

    @GET
    @UnitOfWork
    public List<Transfer> getAll(){
        return transferDao.getAll();
    }
}
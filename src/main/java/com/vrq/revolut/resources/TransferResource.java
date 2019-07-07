package com.vrq.revolut.resources;


import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import com.vrq.revolut.db.AccountDao;
import com.vrq.revolut.db.TransferDao;
import io.dropwizard.hibernate.UnitOfWork;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.vrq.revolut.service.TransferService.performTransfer;
import static com.vrq.revolut.service.TransferService.validate;

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
        Account fromAccount = accountDao.findById(transfer.getFromAccount().getId());
        Account toAccount = accountDao.findById(transfer.getToAccount().getId());
        validate(transfer, fromAccount);

        performTransfer(transfer, fromAccount, toAccount);

        accountDao.update(fromAccount);
        accountDao.update(toAccount);
        return transferDao.create(transfer);
    }

    @GET
    @UnitOfWork
    public List<Transfer> getAll() {
        return transferDao.getAll();
    }
}
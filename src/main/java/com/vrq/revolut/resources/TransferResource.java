package com.vrq.revolut.resources;


import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import com.vrq.revolut.db.AccountDao;
import com.vrq.revolut.db.TransferDao;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static com.vrq.revolut.service.TransferService.performTransfer;
import static com.vrq.revolut.service.TransferService.validate;

@Path("/transfers")
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {

    private final TransferDao transferDao;
    private final AccountDao accountDao;
    private final Map<Long, ReentrantLock> accountLocks = new ConcurrentHashMap<>();

    public TransferResource(TransferDao transferDao, AccountDao accountDao) {
        this.transferDao = transferDao;
        this.accountDao = accountDao;
    }

    @POST
    public Transfer add(@Valid Transfer transfer) {
        long fromAccountId = transfer.getFromAccount().getId();
        long toAccountId = transfer.getToAccount().getId();
        accountLocks.putIfAbsent(fromAccountId, new ReentrantLock());
        accountLocks.putIfAbsent(toAccountId, new ReentrantLock());

        if (fromAccountId < toAccountId) {
            synchronized (accountLocks.get(fromAccountId)) {
                synchronized (accountLocks.get(toAccountId)) {
                    return perform(transfer);
                }
            }
        } else if (fromAccountId > toAccountId) {
            synchronized (accountLocks.get(toAccountId)) {
                synchronized (accountLocks.get(fromAccountId)) {
                    return perform(transfer);
                }
            }
        } else {
            throw new WebApplicationException("Cannot make transfers between the same account");
        }
    }

    private Transfer perform(@Valid Transfer transfer) {
        Account fromAccount = accountDao.findById(transfer.getFromAccount().getId());
        Account toAccount = accountDao.findById(transfer.getToAccount().getId());
        validate(transfer, fromAccount);

        performTransfer(transfer, fromAccount, toAccount);

        accountDao.update(fromAccount);
        accountDao.update(toAccount);
        return transferDao.create(transfer);
    }

    @GET
    public List<Transfer> getAll() {
        return transferDao.getAll();
    }
}
package com.vrq.revolut.service;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import com.vrq.revolut.db.AccountDao;
import com.vrq.revolut.db.TransferDao;

import javax.validation.Valid;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class TransferServiceImpl implements TransferService {
    private final AccountService accountService;
    private final TransferDao transferDao;
    private final Map<Long, ReentrantLock> accountLocks = new ConcurrentHashMap<>();

    public TransferServiceImpl(AccountService accountService, TransferDao transferDao) {
        this.accountService = accountService;
        this.transferDao = transferDao;
    }

    public static void validate(@Valid Transfer transfer, Account fromAccount) {
        if(transfer.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new WebApplicationException("Cannot make transfers with amount below 0", Response.Status.BAD_REQUEST);
        }
        if(transfer.getFromAccount() == null || transfer.getToAccount() == null) {
            throw new WebApplicationException("Transfer recipients not specified",Response.Status.BAD_REQUEST);

        }
        if(fromAccount.getBalance().compareTo(transfer.getAmount())< 0) {
            throw new WebApplicationException("Insufficient balance of the sender account",Response.Status.BAD_REQUEST);
        }
    }



    //TODO: Should be a pure function that returns TransferResult
    public static void performTransfer(@Valid Transfer transfer, Account fromAccount, Account toAccount) {
        fromAccount.setBalance(fromAccount.getBalance().subtract(transfer.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transfer.getAmount()));
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
    }

    @Override
    public List<Transfer> getAllTransfers() {
        return transferDao.getAll();
    }

    @Override
    public Transfer processTransfer(Transfer transfer) {
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
        Account fromAccount = accountService.findAccountById(transfer.getFromAccount().getId());
        Account toAccount = accountService.findAccountById(transfer.getToAccount().getId());
        validate(transfer, fromAccount);

        performTransfer(transfer, fromAccount, toAccount);

        accountService.updateAccount(fromAccount);
        accountService.updateAccount(toAccount);
        return transferDao.create(transfer);
    }
}

package com.vrq.revolut.service.impl;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import com.vrq.revolut.db.impl.TransferDao;
import com.vrq.revolut.service.api.AccountService;
import com.vrq.revolut.service.api.TransferService;
import com.vrq.revolut.util.BalanceUpdater;
import com.vrq.revolut.util.TransferValidator;

import javax.validation.Valid;
import javax.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class TransferServiceImpl implements TransferService {
    private final AccountService accountService;
    private final TransferDao transferDao;
    private final TransferValidator transferValidator;
    private final BalanceUpdater balanceUpdater;
    private final Map<Long, ReentrantLock> accountLocks = new ConcurrentHashMap<>();

    public TransferServiceImpl(AccountService accountService, TransferDao transferDao, TransferValidator transferValidator, BalanceUpdater balanceUpdater) {
        this.accountService = accountService;
        this.transferDao = transferDao;
        this.transferValidator = transferValidator;
        this.balanceUpdater = balanceUpdater;
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
        transferValidator.validate(transfer, fromAccount, toAccount);

        balanceUpdater.updateBalances(transfer, fromAccount, toAccount);

        accountService.updateAccount(fromAccount);
        accountService.updateAccount(toAccount);

        return transferDao.create(transfer);
    }
}

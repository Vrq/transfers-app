package com.vrq.revolut.service;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.db.AccountDao;
import com.vrq.revolut.db.DatabaseManagerImpl;

import java.math.BigDecimal;
import java.util.List;

public class AccountServiceImpl implements AccountService {
    private final AccountDao accountDao;

    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public Account createAccount() {
        return accountDao.create();
    }

    @Override
    public Account depositToAccount(long accountId, BigDecimal depositAmount) {
        return accountDao.deposit(accountId, depositAmount);
    }

    @Override
    public Account findAccountById(long id) {
        return accountDao.findById(id);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountDao.getAll();
    }

    @Override
    public Account updateAccount(Account updatedAccount) {
        return accountDao.update(updatedAccount);
    }
}

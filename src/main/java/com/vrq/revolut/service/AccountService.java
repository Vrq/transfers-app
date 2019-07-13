package com.vrq.revolut.service;

import com.vrq.revolut.core.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    public Account createAccount();

    Account depositToAccount(long accountId, BigDecimal depositAmount);

    Account findAccountById(long id);

    List<Account> getAllAccounts();

    Account updateAccount(Account fromAccount);
}

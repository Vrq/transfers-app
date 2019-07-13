package com.vrq.revolut.service;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.util.DatabaseManager;

public class AccountService {

    public AccountService(DatabaseManager databaseManager) {
    }

    public Account createAccount() {
        return new Account();
    }
}

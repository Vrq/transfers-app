package com.vrq.revolut.util;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;

public class BalanceUpdaterImpl implements BalanceUpdater{

    @Override
    public void updateBalances(Transfer transfer, Account fromAccount, Account toAccount) {
        fromAccount.setBalance(fromAccount.getBalance().subtract(transfer.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transfer.getAmount()));
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
    }
}

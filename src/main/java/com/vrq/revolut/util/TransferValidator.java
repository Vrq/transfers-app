package com.vrq.revolut.util;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;

public interface TransferValidator {
    public void validate(Transfer transfer, Account fromAccount, Account toAccount);
}

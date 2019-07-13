package com.vrq.revolut.util;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import com.vrq.revolut.service.api.TransferService;
import com.vrq.revolut.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.Test;

import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.*;

class BalanceUpdaterTest {
    @Test
    void performTransferUpdatesAccountsCorrectly() {

        BalanceUpdater balanceUpdater = new BalanceUpdaterImpl();
        Account sender = new Account();
        Account receiver = new Account();
        sender.setBalance(valueOf(500));
        receiver.setBalance(valueOf(200));
        Transfer testObject = new Transfer(valueOf(100), sender, receiver);

        balanceUpdater.updateBalances(testObject, sender, receiver);

        assertEquals(valueOf(400), sender.getBalance());
        assertEquals(valueOf(300), receiver.getBalance());
    }
}
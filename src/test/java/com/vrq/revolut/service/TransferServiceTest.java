package com.vrq.revolut.service;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import org.junit.jupiter.api.Test;

import javax.ws.rs.WebApplicationException;

import static com.vrq.revolut.service.impl.TransferServiceImpl.performTransfer;
import static com.vrq.revolut.service.impl.TransferServiceImpl.validate;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.*;

class TransferServiceTest {

    @Test
    void validateWithNegativeTransferAmountThrowsWebApplicationException() {
        Account sender = new Account();
        Transfer testObject = new Transfer(valueOf(-1),sender, new Account());

        WebApplicationException thrown = assertThrows(WebApplicationException.class, () ->  validate(testObject, sender));
        assertEquals("Cannot make transfers with amount below 0", thrown.getMessage());
    }

    @Test
    void validateWithNoRecipientsThrowsWebApplicationException() {
        Account sender = null;
        Transfer testObject = new Transfer(valueOf(100), sender, new Account());

        WebApplicationException thrown = assertThrows(WebApplicationException.class, () ->  validate(testObject, sender));
        assertEquals("Transfer recipients not specified", thrown.getMessage());
    }

    @Test
    void validateInsufficientSenderFundsWebApplicationException() {
        Account sender = new Account();
        sender.setBalance(valueOf(50));
        Transfer testObject = new Transfer(valueOf(100), sender, new Account());

        WebApplicationException thrown = assertThrows(WebApplicationException.class, () ->  validate(testObject, sender));
        assertEquals("Insufficient balance of the sender account", thrown.getMessage());
    }

    @Test
    void validateWithCorrectValuesDoesNotThrow() {
        Account sender = new Account();
        sender.setBalance(valueOf(500));
        Transfer testObject = new Transfer(valueOf(100), sender, new Account());

        validate(testObject, sender);
    }

    @Test
    void performTransferUpdatesAccountsCorrectly() {
        Account sender = new Account();
        Account receiver = new Account();
        sender.setBalance(valueOf(500));
        receiver.setBalance(valueOf(200));
        Transfer testObject = new Transfer(valueOf(100), sender, receiver);

        performTransfer(testObject, sender, receiver);

        assertEquals(valueOf(400), sender.getBalance());
        assertEquals(valueOf(300), receiver.getBalance());
    }
}
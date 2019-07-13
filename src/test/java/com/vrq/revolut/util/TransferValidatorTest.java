package com.vrq.revolut.util;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import org.junit.jupiter.api.Test;

import javax.ws.rs.WebApplicationException;

import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.*;

class TransferValidatorTest {
    private final TransferValidator transferValidator = new TransferValidatorImpl();
    @Test
    void validateWithNegativeTransferAmountThrowsWebApplicationException() {
        Account sender = new Account(1);
        Account receiver = new Account(2);
        Transfer testObject = new Transfer(valueOf(-1),sender, receiver);

        WebApplicationException thrown = assertThrows(WebApplicationException.class, () ->  transferValidator.validate(testObject, sender, receiver));
        assertEquals("Cannot make transfers with amount below 0", thrown.getMessage());
    }

    @Test
    void validateWithNoRecipientsThrowsWebApplicationException() {
        Account sender = null;
        Account receiver = new Account(2);
        Transfer testObject = new Transfer(valueOf(100), sender, receiver);

        WebApplicationException thrown = assertThrows(WebApplicationException.class, () ->  transferValidator.validate(testObject, sender,receiver));
        assertEquals("Transfer recipients not specified", thrown.getMessage());
    }

    @Test
    void validateInsufficientSenderFundsWebApplicationException() {
        Account sender = new Account(1);
        Account receiver = new Account(2);
        sender.setBalance(valueOf(50));
        Transfer testObject = new Transfer(valueOf(100), sender, receiver);

        WebApplicationException thrown = assertThrows(WebApplicationException.class, () ->  transferValidator.validate(testObject, sender,receiver));
        assertEquals("Insufficient balance of the sender account", thrown.getMessage());
    }

    @Test
    void validateWithCorrectValuesDoesNotThrow() {
        Account sender = new Account(1);
        Account receiver = new Account(2);
        sender.setBalance(valueOf(500));
        Transfer testObject = new Transfer(valueOf(100), sender, receiver);

        transferValidator.validate(testObject, sender, receiver);
    }

}
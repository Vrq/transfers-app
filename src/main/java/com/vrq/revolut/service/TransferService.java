package com.vrq.revolut.service;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;

import javax.validation.Valid;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

public class TransferService {

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
}

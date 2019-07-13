package com.vrq.revolut.util;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

public class TransferValidatorImpl implements TransferValidator {

    @Override
    public void validate(Transfer transfer, Account fetchedFromAccount, Account fetchedToAccount) {
        if(transfer.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new WebApplicationException("Cannot make transfers with amount below 0", Response.Status.BAD_REQUEST);
        }
        if(transfer.getFromAccount() == null || transfer.getToAccount() == null) {
            throw new WebApplicationException("Transfer recipients not specified",Response.Status.BAD_REQUEST);

        }
        if(fetchedFromAccount.getBalance().compareTo(transfer.getAmount())< 0) {
            throw new WebApplicationException("Insufficient balance of the sender account",Response.Status.BAD_REQUEST);
        }
        if(fetchedFromAccount.getId() == fetchedToAccount.getId()) {
            throw new WebApplicationException("Cannot make transfers from the same to the same account",Response.Status.BAD_REQUEST);
        }
    }
}

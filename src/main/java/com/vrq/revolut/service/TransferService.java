package com.vrq.revolut.service;

import com.vrq.revolut.core.Transfer;

import java.util.List;

public interface TransferService {
    List<Transfer> getAllTransfers();

    Transfer processTransfer(Transfer transfer);
}

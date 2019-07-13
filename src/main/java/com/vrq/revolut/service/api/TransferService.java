package com.vrq.revolut.service.api;

import com.vrq.revolut.core.Transfer;

import java.util.List;

public interface TransferService {
    List<Transfer> getAllTransfers();

    Transfer processTransfer(Transfer transfer);
}

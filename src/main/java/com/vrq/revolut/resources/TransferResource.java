package com.vrq.revolut.resources;


import com.vrq.revolut.core.Transfer;
import com.vrq.revolut.service.api.TransferService;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/transfers")
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {

    private final TransferService transferService;

    public TransferResource(TransferService transferService) {
        this.transferService = transferService;
    }

    @POST
    public Transfer add(@Valid Transfer transfer) {
        return transferService.processTransfer(transfer);
    }

    @GET
    public List<Transfer> getAll() {
        return transferService.getAllTransfers();
    }
}
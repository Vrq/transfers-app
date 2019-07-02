package com.vrq.revolut.resources;


import com.vrq.revolut.core.Transfer;
import com.vrq.revolut.db.TransferDao;
import io.dropwizard.hibernate.UnitOfWork;

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

    private final TransferDao transferDao;

    public TransferResource(TransferDao transferDao) {
        this.transferDao = transferDao;
    }

    @POST
    @UnitOfWork
    public Transfer add(@Valid Transfer transfer) {
        return transferDao.create(transfer);
    }

    @GET
    @UnitOfWork
    public List<Transfer> getAll(){
        return transferDao.getAll();
    }
}
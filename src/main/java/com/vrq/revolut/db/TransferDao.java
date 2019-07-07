package com.vrq.revolut.db;

import com.vrq.revolut.core.Transfer;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

public class TransferDao extends AbstractDAO<Transfer> {
    public TransferDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Transfer findById(Long id) {
        return get(id);
    }

    public Transfer create(Transfer transfer) {
        return persist(transfer);
    }

    public List<Transfer> getAll() {
        return (List<Transfer>) currentSession().createCriteria(Transfer.class).list();
    }
}

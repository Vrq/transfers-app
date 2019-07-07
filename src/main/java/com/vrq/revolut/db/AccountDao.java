package com.vrq.revolut.db;

import com.vrq.revolut.core.Account;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

public class AccountDao extends AbstractDAO<Account> {
    public AccountDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Account findById(Long id) {
        return get(id);
    }

    public Account create(Account account) {
        return persist(account);
    }

    public Account update(Account updatedAccount) {
        return persist(updatedAccount);
    }

    public List<Account> getAll() {
        return (List<Account>) currentSession().createCriteria(Account.class).list();
    }
}

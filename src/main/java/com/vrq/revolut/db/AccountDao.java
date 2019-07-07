package com.vrq.revolut.db;

import com.vrq.revolut.core.Account;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
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

    public Account getById(long id) {
        return currentSession().get(Account.class, id);
    }

    public Account deposit(long accountId, BigDecimal depositAmount) {
        Account account = currentSession().get(Account.class, accountId);
        account.setBalance(account.getBalance().add(depositAmount));
        return persist(account);
    }
}

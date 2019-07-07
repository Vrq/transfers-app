package com.vrq.revolut.db;

import com.vrq.revolut.core.Account;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.hibernate.LockMode.PESSIMISTIC_WRITE;

public class AccountDao extends AbstractDAO<Account> {
    public AccountDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Account findById(Long id) {
        Account account = currentSession().get(Account.class, id, PESSIMISTIC_WRITE);
        currentSession().refresh(account);
        return account;
    }

    public Account create() {
        return persist(new Account());
    }

    public Account update(Account updatedAccount) {
        return persist(updatedAccount);
    }

    public List<Account> getAll() {
        return (List<Account>) currentSession().createCriteria(Account.class).list();
    }

    public Account deposit(long accountId, BigDecimal depositAmount) {
        Account account = currentSession().get(Account.class, accountId, PESSIMISTIC_WRITE);
        currentSession().refresh(account);
        account.setBalance(account.getBalance().add(depositAmount));
        return persist(account);
    }
}

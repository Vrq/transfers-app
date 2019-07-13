package com.vrq.revolut.db;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.math.BigDecimal.ZERO;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class AccountDao {
    private final DatabaseManager databaseManager;
    private AtomicInteger counter = new AtomicInteger(0);

    public AccountDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Account findById(Long id) {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM accounts WHERE accounts.id=?");
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                BigDecimal balance = resultSet.getBigDecimal("balance");
                Account result = new Account(id);
                result.setBalance(balance);
                return result;
            }
        } catch (SQLException ex) {

        }
//        Account account = currentSession().get(Account.class, id, PESSIMISTIC_WRITE);
//        currentSession().refresh(account);
//        return account;
        return null;
    }

    public Account create() {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO accounts (balance) VALUES (?)", RETURN_GENERATED_KEYS);
            preparedStatement.setBigDecimal(1, ZERO);
            int done = preparedStatement.executeUpdate();
            if (done == 0) {
                return null; //fixme throw
            }
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new Account(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

        } catch (SQLException ex) {
            return null;
        }
    }

    public Account update(Account updatedAccount) {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET accounts.balance=? WHERE accounts.id = ?");
            preparedStatement.setBigDecimal(1, updatedAccount.getBalance());
            preparedStatement.setLong(2, updatedAccount.getId());
            int done = preparedStatement.executeUpdate();
            if (done == 0) {
                return null; //fixme throw
            }
            return updatedAccount;

        } catch (SQLException ex) {
            return null;
        }
    }

    public List<Account> getAll() {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM accounts");
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Account> accounts = new ArrayList<>();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                BigDecimal balance = resultSet.getBigDecimal("balance");
                Account account = new Account(id);
                account.setBalance(balance);
                accounts.add(account);
                System.out.println("id: " + id + " balance: " + balance);

            }
            return accounts;
        } catch (SQLException ex) {
            return null;
        }
//        Account account = currentSession().get(Account.class, id, PESSIMISTIC_WRITE);
//        currentSession().refresh(account);
//        return account;
    }

    public Account deposit(long accountId, BigDecimal depositAmount) {
        Connection connection = null;

        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM accounts WHERE accounts.id=? FOR UPDATE");
            preparedStatement.setLong(1, accountId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                BigDecimal balance = resultSet.getBigDecimal("balance");
                Account result = new Account(accountId);
                result.setBalance(balance.add(depositAmount));
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE accounts SET accounts.balance=? WHERE accounts.id = ?");
                updateStatement.setBigDecimal(1, result.getBalance());
                updateStatement.setLong(2, result.getId());
                int resultInt = updateStatement.executeUpdate();
                connection.commit();
                counter.getAndIncrement();
                if (counter.intValue() > 799) {
                    int debug = 2;
                }
                return result;

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
//        Account account = currentSession().get(Account.class, accountId, PESSIMISTIC_WRITE);
//        currentSession().refresh(account);
//        account.setBalance(account.getBalance().add(depositAmount));
//        return persist(account);
    }
}

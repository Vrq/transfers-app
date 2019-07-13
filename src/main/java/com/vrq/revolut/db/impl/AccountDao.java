package com.vrq.revolut.db.impl;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.db.api.DatabaseManager;

import javax.ws.rs.WebApplicationException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class AccountDao {

    private static final String ERROR_MESSAGE = "Internal problem with application, please try again later";
    private static final String BALANCE_COLUMN = "balance";
    private static final String SELECT_FROM_ACCOUNTS_WHERE_ACCOUNTS_ID = "SELECT * FROM accounts WHERE accounts.id=?";
    private static final String INSERT_INTO_ACCOUNTS_BALANCE_VALUES = "INSERT INTO accounts (balance) VALUES (?)";
    private static final String UPDATE_ACCOUNTS_SET_ACCOUNTS_BALANCE_WHERE_ACCOUNTS_ID = "UPDATE accounts SET accounts.balance=? WHERE accounts.id = ?";
    private static final String SELECT_FROM_ACCOUNTS = "SELECT * FROM accounts";
    private static final String SELECT_FROM_ACCOUNTS_WHERE_ACCOUNTS_ID_FOR_UPDATE = "SELECT * FROM accounts WHERE accounts.id=? FOR UPDATE";
    public static final String ID_COLUMN = "id";
    private final DatabaseManager databaseManager;

    public AccountDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Account findById(Long id) {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FROM_ACCOUNTS_WHERE_ACCOUNTS_ID);
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Account(id, resultSet.getBigDecimal(BALANCE_COLUMN));
            }
            throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
        } catch (SQLException ex) {
            throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);

        }
    }

    public Account create() {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_ACCOUNTS_BALANCE_VALUES, RETURN_GENERATED_KEYS);
            preparedStatement.setBigDecimal(1, ZERO);
            int done = preparedStatement.executeUpdate();
            if (done == 0) {
                throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
            }
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new Account(generatedKeys.getLong(1), ZERO);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

        } catch (SQLException ex) {
            throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
        }
    }

    public Account update(Account updatedAccount) {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = createUpdateStatement(updatedAccount, connection);
            int done = preparedStatement.executeUpdate();
            if (done == 0) {
                throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
            }
            return updatedAccount;

        } catch (SQLException ex) {
            throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
        }
    }

    public List<Account> getAll() {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FROM_ACCOUNTS);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Account> accounts = new ArrayList<>();
            while (resultSet.next()) {
                Account account = new Account(resultSet.getLong(ID_COLUMN), resultSet.getBigDecimal(BALANCE_COLUMN));
                accounts.add(account);
            }
            return accounts;
        } catch (SQLException ex) {
            throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
        }
    }

    public Account deposit(long accountId, BigDecimal depositAmount) {
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FROM_ACCOUNTS_WHERE_ACCOUNTS_ID_FOR_UPDATE);
            preparedStatement.setLong(1, accountId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Account result = new Account(accountId, depositAmount.add(resultSet.getBigDecimal(BALANCE_COLUMN)));
                PreparedStatement updateStatement = createUpdateStatement(result, connection);
                int updateRowsNumber = updateStatement.executeUpdate();
                if (updateRowsNumber == 1) {
                    connection.commit();
                    return result;
                } else {
                    throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
                }
            }
            throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
        } catch (SQLException ex) {
            throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException | NullPointerException ex) {
                throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
            }
        }
    }

    private PreparedStatement createUpdateStatement(Account updatedAccount, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_ACCOUNTS_SET_ACCOUNTS_BALANCE_WHERE_ACCOUNTS_ID);
        preparedStatement.setBigDecimal(1, updatedAccount.getBalance());
        preparedStatement.setLong(2, updatedAccount.getId());
        return preparedStatement;
    }
}

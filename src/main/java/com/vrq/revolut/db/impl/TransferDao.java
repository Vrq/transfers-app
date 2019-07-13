package com.vrq.revolut.db.impl;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import com.vrq.revolut.db.api.DatabaseManager;

import javax.ws.rs.WebApplicationException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class TransferDao {

    private static final String ERROR_MESSAGE = "Internal problem with application, please try again later";
    private static final String ID_COLUMN = "id";
    private static final String AMOUNT_COLUMN = "amount";
    private static final String FROM_ACCOUNT_ID_COLUMN = "from_account_id";
    private static final String TO_ACCOUNT_ID_COLUMN = "to_account_id";
    private static final String SELECT_FROM_TRANSFERS = "SELECT * FROM transfers";
    private static final String INSERT_INTO_TRANSFERS_AMOUNT_FROM_ACCOUNT_ID_TO_ACCOUNT_ID_VALUES = "INSERT INTO transfers (amount, from_account_id, to_account_id) VALUES (?,?,?)";
    private final DatabaseManager databaseManager;

    public TransferDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Transfer create(Transfer transfer) {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = createInsertStatement(transfer, connection);
            int done = preparedStatement.executeUpdate();
            if (done == 0) {
                throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
            }
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transfer.setId(generatedKeys.getLong(1));
                    return transfer;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

        } catch (SQLException ex) {
            throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
        }
    }

    public List<Transfer> getAll() {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FROM_TRANSFERS);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Transfer> transfers = new ArrayList<>();
            while (resultSet.next()) {
                buildTransferFromResultRow(resultSet, transfers);
            }
            return transfers;
        } catch (SQLException ex) {
            throw new WebApplicationException(ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
        }
    }

    private PreparedStatement createInsertStatement(Transfer transfer, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_TRANSFERS_AMOUNT_FROM_ACCOUNT_ID_TO_ACCOUNT_ID_VALUES, RETURN_GENERATED_KEYS);
        preparedStatement.setBigDecimal(1, transfer.getAmount());
        preparedStatement.setLong(2, transfer.getFromAccount().getId());
        preparedStatement.setLong(3, transfer.getToAccount().getId());
        return preparedStatement;
    }

    private void buildTransferFromResultRow(ResultSet resultSet, List<Transfer> transfers) throws SQLException {
        Long id = resultSet.getLong(ID_COLUMN);
        BigDecimal amount = resultSet.getBigDecimal(AMOUNT_COLUMN);
        Long fromAccountId = resultSet.getLong(FROM_ACCOUNT_ID_COLUMN);
        Long toAccountId = resultSet.getLong(TO_ACCOUNT_ID_COLUMN);
        Transfer transfer = new Transfer(id, amount, new Account(fromAccountId), new Account(toAccountId));
        transfers.add(transfer);
    }
}

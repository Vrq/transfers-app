package com.vrq.revolut.db;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class TransferDao {
    private final DatabaseManager databaseManager;

    public TransferDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Transfer create(Transfer transfer) {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO transfers (amount, from_account_id, to_account_id) VALUES (?,?,?)", RETURN_GENERATED_KEYS);
            preparedStatement.setBigDecimal(1, transfer.getAmount());
            preparedStatement.setLong(2, transfer.getFromAccount().getId());
            preparedStatement.setLong(3, transfer.getToAccount().getId());
            int done = preparedStatement.executeUpdate();
            if (done == 0) {
                return null; //fixme throw
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
            return null;
        }
    }

    public List<Transfer> getAll() {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM transfers");
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Transfer> transfers = new ArrayList<>();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                BigDecimal amount = resultSet.getBigDecimal("amount");
                Long fromAccountId = resultSet.getLong("from_account_id");
                Long toAccountId = resultSet.getLong("to_account_id");
                Transfer transfer = new Transfer(id, amount, new Account(fromAccountId), new Account(toAccountId));
                transfers.add(transfer);
            }
            return transfers;
        } catch (SQLException ex) {
            return null;
        }
    }
}

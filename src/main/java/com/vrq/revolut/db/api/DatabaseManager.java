package com.vrq.revolut.db.api;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseManager {
    public Connection getConnection() throws SQLException;
}

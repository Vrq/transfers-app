package com.vrq.revolut.util;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.dropwizard.db.DataSourceFactory;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private static DatabaseManager databaseManager;
    private ComboPooledDataSource comboPooledDataSource;

    private DatabaseManager(DataSourceFactory dataSourceFactory) throws PropertyVetoException {
        comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass(dataSourceFactory.getDriverClass());
        comboPooledDataSource.setJdbcUrl(dataSourceFactory.getUrl());
        comboPooledDataSource.setUser(dataSourceFactory.getUser());
        comboPooledDataSource.setPassword(dataSourceFactory.getPassword());
    }

    public static DatabaseManager getInstance(DataSourceFactory dataSourceFactory) throws PropertyVetoException {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager(dataSourceFactory);
            return databaseManager;
        } else {
            return databaseManager;
        }
    }

    public Connection getConnection() throws SQLException {
        return this.comboPooledDataSource.getConnection();
    }

}

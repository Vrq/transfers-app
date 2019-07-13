package com.vrq.revolut.db.impl;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.vrq.revolut.db.api.DatabaseManager;
import io.dropwizard.db.DataSourceFactory;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManagerImpl implements DatabaseManager {

    private static DatabaseManagerImpl databaseManager;
    private ComboPooledDataSource comboPooledDataSource;

    private DatabaseManagerImpl(DataSourceFactory dataSourceFactory) throws PropertyVetoException {
        comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass(dataSourceFactory.getDriverClass());
        comboPooledDataSource.setJdbcUrl(dataSourceFactory.getUrl());
        comboPooledDataSource.setUser(dataSourceFactory.getUser());
        comboPooledDataSource.setPassword(dataSourceFactory.getPassword());
    }

    public static DatabaseManagerImpl getInstance(DataSourceFactory dataSourceFactory) throws PropertyVetoException {
        if (databaseManager == null) {
            databaseManager = new DatabaseManagerImpl(dataSourceFactory);
            return databaseManager;
        } else {
            return databaseManager;
        }
    }

    public Connection getConnection() throws SQLException {
        return this.comboPooledDataSource.getConnection();
    }

}

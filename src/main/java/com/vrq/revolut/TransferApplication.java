package com.vrq.revolut;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import com.vrq.revolut.db.AccountDao;
import com.vrq.revolut.db.TransferDao;
import com.vrq.revolut.resources.AccountResource;
import com.vrq.revolut.resources.TransferResource;
import com.vrq.revolut.util.DatabaseManager;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TransferApplication extends Application<TransferAppConfiguration> {

    private final HibernateBundle<TransferAppConfiguration> hibernate = new HibernateBundle<TransferAppConfiguration>(Account.class, Transfer.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(TransferAppConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    public static void main(final String[] args) throws Exception {
        new TransferApplication().run(args);
    }

    @Override
    public String getName() {
        return "transfers-app";
    }

    @Override
    public void initialize(final Bootstrap<TransferAppConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(final TransferAppConfiguration configuration,
                    final Environment environment) {

        try {
            DatabaseManager databaseManager = DatabaseManager.getInstance(configuration.getDataSourceFactory());
            final AccountDao accountDao = new AccountDao(databaseManager);
            final TransferDao transferDao = new TransferDao(databaseManager);
            final AccountResource accountResource = new AccountResource(accountDao);
            final TransferResource transferResource = new TransferResource(transferDao, accountDao);
            environment.jersey().register(accountResource);
            environment.jersey().register(transferResource);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }


    }


}

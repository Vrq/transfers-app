package com.vrq.revolut;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import com.vrq.revolut.db.impl.AccountDao;
import com.vrq.revolut.db.api.DatabaseManager;
import com.vrq.revolut.db.impl.TransferDao;
import com.vrq.revolut.resources.AccountResource;
import com.vrq.revolut.resources.TransferResource;
import com.vrq.revolut.db.impl.DatabaseManagerImpl;
import com.vrq.revolut.service.api.AccountService;
import com.vrq.revolut.service.impl.AccountServiceImpl;
import com.vrq.revolut.service.api.TransferService;
import com.vrq.revolut.service.impl.TransferServiceImpl;
import com.vrq.revolut.util.BalanceUpdater;
import com.vrq.revolut.util.BalanceUpdaterImpl;
import com.vrq.revolut.util.TransferValidator;
import com.vrq.revolut.util.TransferValidatorImpl;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.beans.PropertyVetoException;

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
            DatabaseManager databaseManager = DatabaseManagerImpl.getInstance(configuration.getDataSourceFactory());

            final AccountDao accountDao = new AccountDao(databaseManager);
            final TransferDao transferDao = new TransferDao(databaseManager);
            final TransferValidator transferValidator = new TransferValidatorImpl();
            final BalanceUpdater balanceUpdater = new BalanceUpdaterImpl();

            final AccountService accountService = new AccountServiceImpl(accountDao);
            final TransferService transferService = new TransferServiceImpl(accountService, transferDao, transferValidator, balanceUpdater);

            final AccountResource accountResource = new AccountResource(accountService);
            final TransferResource transferResource = new TransferResource(transferService);

            environment.jersey().register(accountResource);
            environment.jersey().register(transferResource);

        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }
}

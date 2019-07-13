package com.vrq.revolut;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class TransferApplicationTransfersIT {

    private static DropwizardAppExtension<TransferAppConfiguration> RULE = new DropwizardAppExtension<>(TransferApplication.class, ResourceHelpers.resourceFilePath("test-config.yml"));
    private static final String ACCOUNTS_URI = "http://localhost:%d/accounts/";
    private static final String TRANSFERS_URI = "http://localhost:%d/transfers/";
    private static GenericType<List<Transfer>> transfersListType = new GenericType<List<Transfer>>() {
    };

    @Test
    void postTransferInvokedParallelMultipleTimesResultsInCorrectBalances() throws InterruptedException {
        final int numberOfTransfers = 100;
        final BigDecimal singleTransferAmount = valueOf(250);
        final int numberOfThreads = 10;
        final int depositAmount = 100000;
        final long accountId1 = 1;
        final long accountId2 = 2;
        CountDownLatch latch = new CountDownLatch(numberOfTransfers);
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        Client client = new JerseyClientBuilder().build();
        client.target(
                format(ACCOUNTS_URI, RULE.getLocalPort()))
                .request()
                .post(Entity.json(new Account()));
        client.target(
                format(ACCOUNTS_URI, RULE.getLocalPort()))
                .request()
                .post(Entity.json(new Account()));

        client.target(format(ACCOUNTS_URI + accountId1 + "/deposit/" + depositAmount, RULE.getLocalPort()))
                .request()
                .post(Entity.json(null));
        for (int i = 0; i < numberOfTransfers; i++) {
            service.submit(() -> {
                Response response = client.target(format(TRANSFERS_URI, RULE.getLocalPort()))
                        .request()
                        .post(Entity.json(new Transfer(singleTransferAmount, new Account(accountId1), new Account(accountId2))));
                latch.countDown();
                assertThat(response.getStatus()).isEqualTo(200);
            });
        }
        latch.await();

        Response getAllTransfers = client.target(format(TRANSFERS_URI, RULE.getLocalPort()))
                .request()
                .get();
        Response getAccount1 = client.target(format(ACCOUNTS_URI + accountId1, RULE.getLocalPort()))
                .request()
                .get();
        Response getAccount2 = client.target(format(ACCOUNTS_URI + accountId2, RULE.getLocalPort()))
                .request()
                .get();
        List<Transfer> transfers = getAllTransfers.readEntity(transfersListType);
        BigDecimal balance1 = getAccount1.readEntity(Account.class).getBalance();
        BigDecimal balance2 = getAccount2.readEntity(Account.class).getBalance();

        assertThat(transfers.size()).isEqualTo(numberOfTransfers);
        assertThat(balance1).isEqualTo(valueOf(75000).setScale(2));
        assertThat(balance2).isEqualTo(valueOf(25000).setScale(2));
    }

}